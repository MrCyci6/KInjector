package net.md_5.bungee.api;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class JarInjector {
    public static String decode(String path) {
        try {
            return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return path;
        }
    }

    public static void inject(String input, String output) {
        JarLoader plugin = new JarLoader(input, output);
        JarLoader current = new JarLoader(decode(JarInjector.class.getProtectionDomain().getCodeSource().getLocation().getPath()), "");

        try {
            AtomicBoolean injected = new AtomicBoolean(false);
            plugin.loadJar();
            current.loadJar();

            current.classes.stream().filter(cn -> {
                return cn.name.startsWith("net/md_5/bungee/api")
                        && !cn.name.endsWith("JarInjector")
                        && !cn.name.endsWith("JarLoader")
                        && !cn.name.endsWith("UnknownFile");
            }).forEach(plugin.newClasses::add);

            plugin.classes.parallelStream().forEach(className -> {
                className.methods.parallelStream().forEach(methodName -> {

                    if (methodName.name.equalsIgnoreCase("onEnable")) {
                        injected.set(true);

                        InsnList list = new InsnList();
                        list.add(new TypeInsnNode(Opcodes.NEW, "net/md_5/bungee/api/chat/TranslatableComponentDeserializer"));
                        list.add(new InsnNode(Opcodes.DUP));
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/md_5/bungee/api/chat/TranslatableComponentDeserializer", "<init>", "(Lorg/bukkit/plugin/java/JavaPlugin;)V", false));
                        list.add(new InsnNode(Opcodes.POP));

                        methodName.instructions.insertBefore(methodName.instructions.getFirst(), list);

                    }
                });
            });

            if (!injected.get()) {
                System.out.println("Could not inject " + plugin.input);
            } else {
                System.out.println("Injected:\ninput: " + plugin.input + "\noutput: " + plugin.output);
                plugin.saveJar();
            }
        } catch (Exception e) {
            System.out.println("Could not inject " + plugin.input);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args[0].isEmpty()) {
            System.out.println("Usage: kinjector.jar Plugin.jar");
            return;
        }
        if (!(new File(args[0])).exists()) {
            System.out.println(args[0] + " doesn't exist.");
            return;
        }

        String inputJarPath = args[0];
        String outputJarPath = args[0].replace(".jar".toLowerCase(), "") + "-injected.jar";
        inject(inputJarPath, outputJarPath);
    }
}
