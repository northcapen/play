package play.kotlin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.config.Services;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KotlinPlugin extends PlayPlugin {

    public boolean compileSources() {
        MessageCollector messageCollector = new MessageCollector() {
            @Override
            public void report(@NotNull CompilerMessageSeverity severity, @NotNull String message, @NotNull CompilerMessageLocation location) {
                String path = location.getPath();
                String position = path == null ? "" : path + ": (" + (location.getLine() + ", " + location.getColumn()) + ") ";

                String text = position + message;

                if (CompilerMessageSeverity.VERBOSE.contains(severity)) {
                    System.out.println("VERBOSE" + text);
                } else if (CompilerMessageSeverity.ERRORS.contains(severity)) {
                    System.out.println("ERRORS" + text);
                } else if (severity == CompilerMessageSeverity.INFO) {
                    System.out.println("INFO" + text);
                } else {
                    System.out.println("VERBOSE" + text);
                }
            }
        };


        K2JVMCompiler k2JVMCompiler = new K2JVMCompiler();
        K2JVMCompilerArguments arguments = new K2JVMCompilerArguments();

        List<VirtualFile> javaPath = Play.javaPath;
        for (VirtualFile virtualFile : javaPath) {
            arguments.freeArgs.add(virtualFile.getRealFile().getAbsolutePath());
        }
        arguments.destination = Play.tmpDir.getAbsolutePath() + "/kotlin";
        arguments.classpath = System.getProperty("java.class.path");
        ExitCode exec = k2JVMCompiler.exec(messageCollector, Services.EMPTY, arguments);
        if(exec == ExitCode.OK) {
            System.out.println("Success!");
            try {
                Collection<File> compileFiles = FileUtils.listFiles(new File(arguments.destination), new String[]{"class"}, true);
                for (File classFile : compileFiles) {
                    byte[] result = IOUtils.toByteArray(new FileInputStream(classFile));
                    String name = "models." + classFile.getName().replace(".class", "");

                    VirtualFile ktFile = VirtualFile.fromRelativePath("/app/" + name.replace(".", "/") + ".kt");
                    Play.classes.add(new ApplicationClasses.ApplicationClass(name, ktFile));
                    Play.classes.getApplicationClass(name).compiled(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException();
        }
        return false;
    }


    @Override
    public boolean detectClassesChange() {
        boolean recompile = false;
        for (ApplicationClasses.ApplicationClass applicationClass : Play.classes.all()) {
            if (applicationClass.timestamp < applicationClass.javaFile.lastModified() && applicationClass.javaFile.getName().endsWith(".kt")) {
                recompile = true;
                break;
            }
        }
        if(recompile) {
            compileSources();
            throw new RuntimeException("Need reload");
        }
        return false;
    }
}
