import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.SpecDoubleInRange;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.electronwill.nightconfig.core.conversion.SpecNotNull;
import com.electronwill.nightconfig.core.conversion.SpecStringInArray;
import com.electronwill.nightconfig.core.utils.StringUtils;
import java.util.List;

/**
 * @author TheElectronWill
 */
public class ObjectConverterAnnotationsExample {
	public static void main(String[] args) {
		// Creates a config with some values:
		Config config = Config.inMemory();
		config.set("the.long.path.in.config", "theValue");
		config.set("e", Math.E);
		config.set("number", 125);
		config.set("name", "A");
		config.set("some", "...");
		config.set("point", "(1,2)");
		System.out.println("Config: " + config);

		// Converts the config to an object:
		ConfigData object = new ObjectConverter().toObject(config, ConfigData::new);
		System.out.println("Object: " + object);

		// Converts the object to a config:
		Config configFromObject = new ObjectConverter().toConfig(object, Config::inMemory);
		System.out.println("Config from object: " + configFromObject);

		/* Explanations:
		   - @Path defines that the corresponding value in the config is at the specified path
		   instead of the field's name
		   - @Spec...InRange specifies that the field's value must be in the given range (min
		   and max are included), like ConfigSpec#defineInRange. If the value isn't correct, an
		   exception is thrown by the toObject method.
		   - @SpecStringInArray specifies that the field's value must be contained in the given
		   array. If the value isn't correct, an exception is thrown by the toObject method.
		   - @SpecNotNull specifies that the field's value must not be null. Otherwise an
		   exception is thrown like with the other @Spec... annotations.
		   - @Conversion defines a conversion between the config value and the field. A
		    new instance of the specified class is created when calling toObject, therefore the
		    class need to have a constructor without arguments (access level doesn't matter: it
		    can be private)
		 */
	}

	static class ConfigData {
		@Path("e")
		@SpecDoubleInRange(min = 2.718, max = 2.719)
		double eulerNumber;

		@Path("number")
		@SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
		int anInt;

		@SpecStringInArray({"A", "B", "C"})
		String name;

		@Path("the.long.path.in.config")
		String value;

		@SpecNotNull
		Object some;

		@Conversion(PointToStringConverter.class)
		Point2D point;

		@Override
		public String toString() {
			return "ConfigData{"
				   + "eulerNumber="
				   + eulerNumber
				   + ", anInt="
				   + anInt
				   + ", name='"
				   + name
				   + '\''
				   + ", value='"
				   + value
				   + '\''
				   + ", some="
				   + some
				   + ", point="
				   + point
				   + '}';
		}
	}

	static class Point2D {
		final int x, y;

		Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Point2D{" + "x=" + x + ", y=" + y + '}';
		}
	}

	static class PointToStringConverter implements Converter<Point2D, String> {
		private PointToStringConverter() {}

		@Override
		public Point2D convertToField(String value) {
			value = value.substring(1, value.length() - 1);
			List<String> splitted = StringUtils.split(value, ',');
			int x = Integer.parseInt(splitted.get(0));
			int y = Integer.parseInt(splitted.get(1));
			return new Point2D(x, y);
		}

		@Override
		public String convertFromField(Point2D value) {
			return "(" + value.x + "," + value.y + ")";
		}
	}
}