package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {



        System.out.println(testJavaCode("""
                public class Main{
                     public static void main(String[] args){
                          System.out.println("JAVA");
                          System.out.println("JAVA");
                          System.out.println("JAVA");
                          System.out.println("JAVA");
                     }
                }
                """,
                """
                JAVA
                JAVA
                JAVA
                JAVA
                """));

    }


    private static String testJavaCode(String code, String target) throws Exception {
        String output = "";

        Path javaFile = createMainClass(Path.of("java-code"), code);

         Process process = compileCode(javaFile);
         if(process != null && (process = runCode(javaFile)) != null){

                 // Read the output
                 output = readProcessMessage(process.getInputStream());

                 if(output.equals(target)) output = "Accepted";
                 else output = "Wrong Answer";

                 process.destroy();
         }
        // Clean up the temporary directory
        deleteDirectory(javaFile.getParent());

        // Return the output
        return output;
    }

    public static Path createMainClass(Path path, String code) throws IOException {
        if(!Files.isExecutable(path)) {
            path = Files.createDirectory(path);
        }
        Path mainClass = path.resolve("Main.java");
        Files.write(mainClass, code.getBytes());
        return mainClass;
    }

    public static Process compileCode(Path path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("javac", "..\\" + path.toString());
        processBuilder.directory(path.getParent().toFile()); // work with this file
        Process process = processBuilder.start();

        // code Validation
        if(process.waitFor() != 0){
            System.out.println(readProcessMessage(process.getErrorStream()));
            return null;
        }
        else
            System.out.println("Code Compiled");

        return process;
    }

    public static Process runCode(Path path) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "Main");
        processBuilder.directory(path.getParent().toFile());
        Process process =  processBuilder.start();

        // Validate Code
        if (!process.waitFor(5, TimeUnit.SECONDS) ){
             System.out.println("Time Limit Exceeded");
            return null;
        } else if (readProcessMessage(process.getErrorStream()).contains("Exception")){
            System.out.println("Runtime Error");
            return null;
        }
        else
            System.out.println("Execution Completed");

        return process;
    }

    public static String readProcessMessage(InputStream inputStream) throws IOException {
        String line;
        String output = "";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = bufferedReader.readLine()) != null){
            output += line + '\n';
        }
        return output;
    }

    public static void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }


}

