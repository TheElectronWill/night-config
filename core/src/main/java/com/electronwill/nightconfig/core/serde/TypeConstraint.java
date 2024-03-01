package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a type constraint that must be satisfied by the serialization/deserialization process.
 */
public final class TypeConstraint {

	// todo later: allow to build nested parameterized types?
	// such as TypeConstraint.Map(String.class, Map(SubKey.class, SubValue.class))
	// such as TypeConstraint.List(ofList(SubValue.class))

	static TypeConstraint[] mapArray(Type[] t) {
		TypeConstraint[] c = new TypeConstraint[t.length];
		for (int i = 0; i < t.length; i++) {
			c[i] = new TypeConstraint(t[i]);
		}
		return c;
	}

	private final Type fullType;
	private Optional<Class<?>> rawClass = null;

	/**
	 * Creates a new TypeConstraint from a {@link Type}.
	 * 
	 * @param fullType the type to match
	 */
	public TypeConstraint(Type fullType) {
		this.fullType = fullType;
	}

	/**
	 * Returns a {@link Type} that represents the full type constraint.
	 * <p>
	 * For instance, it can return a {@link WildcardType} with lower and/or upper bounds, like
	 * {@code ? extends Bound},
	 * or a parameterized type with generic type arguments like {@code Map<String, ? extends Iterable<Object>>}.
	 * 
	 * @return the full type constraint
	 */
	public Type getFullType() {
		return fullType;
	}

	/**
	 * Returns a {@link Class} that can satisfy the type constraint, if it can be found.
	 * If no such class can be found, an empty optional is returned.
	 * <p>
	 * An empty optional can be returned if the type constraint is a wildcard type or a type variable with
	 * multiple bounds, such as {@code ? super B1 extends B2} or {@code T extends B1 & B2}, in which case it's not
	 * enough to select a bound, one must find a satisfying intersection.
	 * It would be difficult to know every possible class, <em>and</em> there could be multiple solutions anyway.
	 * 
	 * @return a class that can satisfy the type constraint, if it can be found
	 */
	public Optional<Class<?>> getSatisfyingRawType() {
		if (rawClass == null) {
			rawClass = Optional.ofNullable(findSatisfyingRawType(fullType));
		}
		return rawClass;
	}

	/**
	 * Inspects this type constraint and attempts to resolve the type arguments that have been applied to the
	 * given class, inside of the constraint. Returns an empty optional if not found.
	 * <p>
	 * For instance, given a class like this:
	 *
	 * <pre>
	 * <code>
	 * class Cls extends {@code MyCollection<String>} {}
	 * class MyCollection implements {@code Collection<String>} {}
	 * </code>
	 * </pre>
	 *
	 * You can expect the following result:
	 *
	 * <pre>
	 * <code>
	 * TypeConstraint t = new TypeConstraint(Cls.class);
	 * {@code Optional<TypeConstraint[]>} args = t.resolveTypeArgumentsFor(Collection.class);
	 * Type collectionValueType = args.get()[0].getFullType();
	 * assertEquals(String.class, collectionValueType);
	 * </code>
	 * </pre>
	 * <p>
	 * It can also resolve type variables used in field declarations.
	 *
	 * <pre>
	 * <code>
	 * class {@code Cls<T extends Collection<String> & OtherBound>} {
	 *     T field;
	 * }
	 * </code>
	 * </pre>
	 *
	 * <pre>
	 * <code>
	 * Field f = Cls.class.getDeclaredField("field");
	 * TypeConstraint t = new TypeConstraint(f.getGenericType());
	 * {@code Optional<TypeConstraint[]>} args = t.resolveTypeArgumentsFor(Collection.class);
	 * Type collectionValueType = args.get()[0].getFullType();
	 * assertEquals(String.class, collectionValueType);
	 * </code>
	 * </pre>
	 * 
	 * @param classToFind the class to find
	 * @return the actual type arguments provided to the given class, if found
	 */
	public Optional<TypeConstraint[]> resolveTypeArgumentsFor(Class<?> classToFind) {
		return Optional.ofNullable(resolveTypeArgumentsFor(fullType, classToFind, new HashMap<>()));
	}

	@Override
	public String toString() {
		return String.format("TypeConstraint[%s, rawType=%s]", fullType, getSatisfyingRawType());
	}

	private static final Class<?> findSatisfyingRawType(Type t) {
		if (t instanceof Class) {
			return (Class<?>) t;
		}
		if (t instanceof ParameterizedType) {
			return findSatisfyingRawType(((ParameterizedType) t).getRawType());
		}
		if (t instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) t).getGenericComponentType();
			Class<?> componentClass = findSatisfyingRawType(componentType);
			if (componentClass == null) {
				return null;
			}
			return Array.newInstance(componentClass, 0).getClass();
		}
		if (t instanceof WildcardType) {
			// For regular WildcardType returned by the reflection API, only one lower or one upper bound is possible.
			// But we also have a custom class RefinedWildcard, which can have multiple bounds.
			WildcardType w = (WildcardType) t;

			Type[] lowerBounds = w.getLowerBounds();
			Type[] upperBounds = w.getUpperBounds();

			if (upperBounds.length == 1) {
				Type upper = upperBounds[0];
				if (lowerBounds.length == 1 && upper == Object.class) {
					return findSatisfyingRawType(lowerBounds[0]);
				} else {
					return findSatisfyingRawType(upper);
				}
			} else if (lowerBounds.length == 1 && upperBounds.length == 0) {
				return findSatisfyingRawType(lowerBounds[0]);
			} else {
				// multiple bounds, we can't compute their intersection
				return null;
			}
		}
		if (t instanceof TypeVariable) {
			// Here, there can be multiple bounds, such as in <T extends B1 & B2>, but we
			// cannot return a single class in that case
			Type[] bounds = ((TypeVariable<?>) t).getBounds();
			if (bounds.length == 1) {
				return findSatisfyingRawType(bounds[0]);
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * Finds the generic type arguments applied to class {@code classToFind} for the
	 * type {@code t}.
	 * 
	 * @see TypeConstraint#resolveTypeArgumentsFor(Class)
	 */
	private static TypeConstraint[] resolveTypeArgumentsFor(Type t, Class<?> classToFind,
			Map<TypeVariable<?>, Type> resolvedVariables) {
		if (t instanceof Class) {
			// "raw" class without type arguments
			if (t == classToFind) {
				return null;
			} else {
				// the type arguments could be in the declaration of the supertypes of the class
				return findParent((Class<?>) t,
						parent -> resolveTypeArgumentsFor(parent, classToFind, resolvedVariables));
			}
		}
		if (t instanceof ParameterizedType) {
			// type with generic parameters such as Cls<A, B>
			ParameterizedType pt = (ParameterizedType) t;
			Type rawType = pt.getRawType();
			Type[] actualTypeArgs = pt.getActualTypeArguments();

			// resolve the args that are variables, and
			// restrict the bounds of wildcards that are less restrictive than the declaration of the type parameter
			for (int i = 0; i < actualTypeArgs.length; i++) {
				Type typeArg = actualTypeArgs[i];
				if (typeArg instanceof WildcardType) {
					// refine wildcard, otherwise we can lose some information on the bounds when we have a field
					// declared as `MyType<?>` with `class MyType<T extends Bound>`

					WildcardType wildcard = (WildcardType) typeArg;

					@SuppressWarnings("unchecked")
					Class<Object> cls = (Class<Object>) rawType;
					TypeVariable<Class<Object>> declaredTypeParam = cls.getTypeParameters()[i];

					actualTypeArgs[i] = refineWildcard(wildcard, declaredTypeParam,
							resolvedVariables);
				} else {
					// if typeArg is a TypeVariable, try to resolve it with the Map of variables
					actualTypeArgs[i] = resolveIfVariable(typeArg, resolvedVariables);
				}
			}

			if (rawType == classToFind) {
				// extract the type parameters of the class we're looking for
				return TypeConstraint.mapArray(actualTypeArgs);
			} else {
				// this is not the class we're looking for
				// remember the actual type arguments.
				TypeVariable<?>[] declaredTypeArgs = ((Class<?>) rawType).getTypeParameters();
				for (int i = 0; i < declaredTypeArgs.length; i++) {
					resolvedVariables.put(declaredTypeArgs[i], actualTypeArgs[i]);
				}
				// recursively search in supertypes (parent class and interfaces)
				return findParent((Class<?>) rawType,
						parent -> resolveTypeArgumentsFor(parent, classToFind, resolvedVariables));
			}
		}
		if (t instanceof TypeVariable) {
			// type variable, probably used in a field declaration, for example:
			// class Cls<T> {
			// T field; // *here*
			// }
			Type[] bounds = ((TypeVariable<?>) t).getBounds();
			TypeConstraint[] res = null;
			for (Type bound : bounds) {
				bound = resolveIfVariable(bound, resolvedVariables);
				res = resolveTypeArgumentsFor(bound, classToFind, resolvedVariables);
				if (res != null) {
					break;
				}
			}
			return res;
		}
		if (t instanceof WildcardType) {
			WildcardType w = (WildcardType) t;
			TypeConstraint[] res = null;
			for (Type bound : w.getUpperBounds()) {
				res = resolveTypeArgumentsFor(resolveIfVariable(bound, resolvedVariables),
						classToFind, resolvedVariables);
				if (res != null) {
					return res;
				}
			}
			for (Type bound : w.getLowerBounds()) {
				res = resolveTypeArgumentsFor(resolveIfVariable(bound, resolvedVariables),
						classToFind, resolvedVariables);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	private static Type resolveIfVariable(Type t, Map<TypeVariable<?>, Type> resolvedVariables) {
		if (t instanceof TypeVariable) {
			Type resolved = resolvedVariables.get(t);
			if (resolved != null) {
				return resolved;
			}
		}
		return t;
	}

	private static <R> R findParent(Class<?> cls, Function<Type, R> f) {
		R res = null;
		Type parentClass = cls.getGenericSuperclass();
		if (parentClass != null) {
			res = f.apply(parentClass);
		}
		if (res == null) {
			Type[] parentInterfaces = cls.getGenericInterfaces();
			for (Type parent : parentInterfaces) {
				res = f.apply(parent);
				if (res != null) {
					break;
				}
			}
		}
		return res;
	}

	private static Type wildcardLowerBound(WildcardType t) {
		Type[] bounds = t.getLowerBounds();
		if (bounds.length > 0) {
			return bounds[0];
		}
		return null;
	}

	private static Type wildcardUpperBound(WildcardType t) {
		Type[] bounds = t.getUpperBounds();
		if (bounds.length > 0) {
			return bounds[0];
		}
		return null;
	}

	static Type refineWildcard(WildcardType wildcard,
			TypeVariable<Class<Object>> declaredTypeParam,
			Map<TypeVariable<?>, Type> resolvedVariables) {

		if (wildcard instanceof RefinedWildcard) {
			return (RefinedWildcard) wildcard;
		}

		// actual: `? extends B` or `? super B`
		Type upperBound = wildcardUpperBound(wildcard);
		Type[] lowerBounds = wildcard.getLowerBounds(); // note: has only one element

		// declared: `? extends B1 & B2 & ...`
		Type[] declaredUpperBounds = declaredTypeParam.getBounds();

		if (declaredUpperBounds.length == 0 || declaredUpperBounds.length == 1
				&& (declaredUpperBounds[0] == Object.class
						|| declaredUpperBounds[0] == upperBound)) {
			// nothing to refine
			return wildcard;
		}

		Type[] refinedLower = lowerBounds;
		Type[] refinedUpper;
		if (upperBound == null || upperBound == Object.class) {
			refinedUpper = declaredUpperBounds;
		} else {
			List<Type> upper = new ArrayList<>(declaredUpperBounds.length + 1);
			upper.add(upperBound);
			for (int i = 0; i < declaredUpperBounds.length; i++) {
				Type declaredUpper = declaredUpperBounds[i];
				if (upperBound != declaredUpper) {
					upper.add(declaredUpper);
				}
			}
			refinedUpper = upper.toArray(new Type[upper.size()]);
		}
		// collapse if upper == lower
		if (refinedLower.length == 1 && refinedUpper.length == 1
				&& refinedLower[0].equals(refinedUpper[0])) {
			return refinedLower[0];
		}
		return new RefinedWildcard(refinedLower, refinedUpper);
	}

	static final class RefinedWildcard implements WildcardType {

		private final Type[] lowerBounds, upperBounds;

		RefinedWildcard(Type[] lowerBounds, Type[] upperBounds) {
			this.lowerBounds = lowerBounds;
			this.upperBounds = upperBounds;
		}

		@Override
		public Type[] getLowerBounds() {
			return lowerBounds;
		}

		@Override
		public Type[] getUpperBounds() {
			return upperBounds;
		}

		@Override
		public String toString() {
			String lower = lowerBounds.length == 0 ? "" : " >: " + Arrays.asList(lowerBounds);
			String upper = "<: " + Arrays.asList(upperBounds);
			return "?" + lower + " " + upper;
		}
	}

	/** A manually-created instance of ParameterizedType. */
	static final class ManuallyParameterized implements ParameterizedType {
		private final Type rawType;
		private final Type[] arguments;

		public ManuallyParameterized(Type rawType, Type... arguments) {
			this.rawType = Objects.requireNonNull(rawType);
			this.arguments = arguments;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return arguments;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public String toString() {
			if (arguments.length == 0) {
				return rawType.toString();
			}
			return rawType + "<" +
					String.join(", ", Arrays.stream(arguments).map(t -> t.toString())
							.toArray(size -> new String[size]))
					+ ">";
		}
	}
}
