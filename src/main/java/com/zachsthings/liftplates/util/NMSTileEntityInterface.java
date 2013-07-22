package com.zachsthings.liftplates.util;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zml2008
 */
public class NMSTileEntityInterface {
	private static final String CRAFTBUKKIT_PREFIX = "org.bukkit.craftbukkit",
			NMS_PREFIX = "net.minecraft.server";
	private static final String VERSION;

	static {
		Class serverClass = Bukkit.getServer().getClass();
		if (!serverClass.getSimpleName().equals("CraftServer")) {
			VERSION = null;
		} else if (serverClass.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
			VERSION = ".";
		} else {
			String name = serverClass.getName();
			name = name.substring("org.bukkit.craftbukkit".length());
			name = name.substring(0, name.length() - "CraftServer".length());
			VERSION = name;
		}
	}

	/**
	 * Get the versioned class name from a class name without the o.b.c prefix.
	 *
	 * @param simpleName The name of the class without the "org.bukkit.craftbukkit" prefix
	 * @return The versioned class name, or {@code null} if not CraftBukkit.
	 */
	public static String getCBClassName(String simpleName) {
		if (VERSION == null) {
			return null;
		}

		return CRAFTBUKKIT_PREFIX + VERSION + simpleName;
	}

	public static String getNMSClassName(String simpleName) {
		if (VERSION == null) {
			return null;
		}

		return NMS_PREFIX + VERSION + simpleName;
	}

	private static Class<?> NMS_WORLD, NMS_TILE_ENTITY, NMS_NBT_COMPOUND;
	private static Constructor<?> NBT_COMPOUND_CONSTRUCTOR;
	private static Method CB_GET_HANDLE, NMS_GET_TILE_ENTITY,
			NMS_TILE_ENTITY_READ, NMS_TILE_ENTITY_WRITE;
	private static Field NMS_TILE_ENTITY_X, NMS_TILE_ENTITY_Y, NMS_TILE_ENTITY_Z;
	private static boolean useNms = true;

	static {
		try {
			NMS_WORLD = Class.forName(getNMSClassName("World"));
			NMS_TILE_ENTITY = Class.forName(getNMSClassName("TileEntity"));
			NMS_NBT_COMPOUND = Class.forName(getNMSClassName("NBTTagCompound"));
			Class<?> cbWorldClass = Class.forName(getCBClassName("CraftWorld"));

			NBT_COMPOUND_CONSTRUCTOR = NMS_NBT_COMPOUND.getConstructor();
			Constructor<?> tentConstructor = NMS_TILE_ENTITY.getConstructor();

			CB_GET_HANDLE = cbWorldClass.getMethod("getHandle");
			NMS_GET_TILE_ENTITY = NMS_WORLD.getMethod("getTileEntity", int.class, int.class, int.class);
			// Detect read/write methods from tile entity -- this is a bit messy but these are the only still-obfuscated method names that we use, so this helps to maintain version compatibility
			// The one that throws an exception with an empty NBTTagCompound is the one we're trying to read from
			Object testTent = tentConstructor.newInstance();
			List<Method> matchingMethods = new ArrayList<Method>(2);

			for (Method m : NMS_TILE_ENTITY.getMethods()) {
				if (!(m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(NMS_NBT_COMPOUND))) {
					continue;
				}
				if (m.getReturnType() != void.class) {
					continue;
				}
				if (matchingMethods.size() >= 2) {
					throw new RuntimeException("More than two methods matching signature public void TileEntity.___(NBTTagCompound)!");
				}
				matchingMethods.add(m);
			}

			if (matchingMethods.size() < 2) {
				throw new RuntimeException("Not 2 methods matched signature public void TileEntity.___(NBTTagCompound)");
			}
			Object testCompound = NBT_COMPOUND_CONSTRUCTOR.newInstance();
			try {
				matchingMethods.get(0).invoke(testTent, testCompound);
				NMS_TILE_ENTITY_WRITE = matchingMethods.get(0); // No exception thrown, write method is first in list
				NMS_TILE_ENTITY_READ = matchingMethods.get(1);
			} catch (InvocationTargetException ex) {
				NMS_TILE_ENTITY_READ = matchingMethods.get(1); // Exception thrown, read method is first in list
				NMS_TILE_ENTITY_WRITE = matchingMethods.get(0);
			}

			NMS_TILE_ENTITY_X = NMS_TILE_ENTITY.getField("x");
			NMS_TILE_ENTITY_Y = NMS_TILE_ENTITY.getField("y");
			NMS_TILE_ENTITY_Z = NMS_TILE_ENTITY.getField("z");
		} catch (Exception e) {
			e.printStackTrace();
			useNms = false;
		}
	}

	public static class TEntWrapper {
		private final Object data;

		TEntWrapper(Object data) {
			this.data = data;
		}

		Object getData() {
			return this.data;
		}
	}


	private static Object createNBTCompound() {
		if (!useNms) {
			return null;
		}

		try {
			return NBT_COMPOUND_CONSTRUCTOR.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

	public static Object tileEntityFromBlock(Block block) {
		if (!useNms) {
			return null;
		}

		try {
			Object nmsWorld = CB_GET_HANDLE.invoke(block.getWorld()); // NMS world from CB world
			return NMS_GET_TILE_ENTITY.invoke(nmsWorld, block.getX(), block.getY(), block.getZ()); // TEnt object from NMS world
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

	public static TEntWrapper getData(Block block) {
		if (!useNms) {
			return null;
		}

		Object tent = tileEntityFromBlock(block);
		if (tent != null && NMS_TILE_ENTITY_WRITE != null) {
			Object tag = createNBTCompound();
			try {
				NMS_TILE_ENTITY_WRITE.invoke(tent, tag); // Store data in compound
			} catch (IllegalAccessException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			}
			return new TEntWrapper(tag);
		} else {
			return null;
		}
	}

	public static void applyData(TEntWrapper data, Block block) {
		if (!useNms) {
			return;
		}

		Object tent = tileEntityFromBlock(block);
		if (tent != null) {
			try {
				int x = NMS_TILE_ENTITY_X.getInt(tent), // Store current coords of tent
						y = NMS_TILE_ENTITY_Y.getInt(tent),
						z = NMS_TILE_ENTITY_Z.getInt(tent);

				NMS_TILE_ENTITY_READ.invoke(tent, data.getData()); // Read stored data into tent

				NMS_TILE_ENTITY_X.setInt(tent, x); // Restore coords of tent
				NMS_TILE_ENTITY_Y.setInt(tent, y);
				NMS_TILE_ENTITY_Z.setInt(tent, z);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
