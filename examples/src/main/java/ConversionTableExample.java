import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ConversionTable;
import com.electronwill.nightconfig.core.utils.StringUtils;
import java.util.List;

/**
 * @author TheElectronWill
 */
public class ConversionTableExample {
	public static void main(String[] args) {
		// Creates a config with some value:
		Config config = Config.inMemory();
		config.set("coordinates", "0,0,0");

		// Creates a conversion table:
		ConversionTable conversionTable = new ConversionTable();
		// Adds a conversion that converts every String to an instance of Coordinates
		conversionTable.put(String.class, (String s) -> {
			List<String> splitted = StringUtils.split(s, ',');
			int x = Integer.parseInt(splitted.get(0));
			int y = Integer.parseInt(splitted.get(1));
			int z = Integer.parseInt(splitted.get(2));
			return new Coordinates(x, y, z);
		});

		// Gets a "wrapped" config that converts the value "just in time" when they are read:
		Config wrappedConfig = conversionTable.wrapRead(config);
		System.out.println("Original config: " + config);
		System.out.println("Wrapped config: " + wrappedConfig);
		System.out.println(
				"Type of 'coordinates' in the original config: " + config.get("coordinates")
																		 .getClass());
		System.out.println(
				"Type of 'coordinates' in the wrapped config: " + wrappedConfig.get("coordinates")
																			   .getClass());

		// You can also convert the config "in-place", replacing its values with the converted ones:
		conversionTable.convertShallow(config);
		System.out.println("Config after conversion: " + config);
	}

	static class Coordinates {
		private final int x, y, z;

		Coordinates(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String toString() {
			return "Coordinates(" + "x=" + x + ", y=" + y + ", z=" + z + ')';
		}
	}
}