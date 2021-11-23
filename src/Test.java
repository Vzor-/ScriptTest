import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
    public static void main(String ... args) {
        try {
            System.out.println("Python 2 ----------------------------");
            runPy2(Paths.get("scripts/", "helloworld.py"));
            System.out.println("Python 3 ----------------------------");
            runPy3(Paths.get("scripts/", "helloworld.py"));
            System.out.println("Visual Basic Script -----------------");
            runVbs(Paths.get("scripts/", "helloworld.vbs"));
            System.out.println("PowerShell Script -------------------");
            runPs1(Paths.get("scripts/", "helloworld.ps1"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void runVbs(Path script) throws IOException, InterruptedException {
        Path cscript = Paths.get(System.getenv("WINDIR"), "system32", "cscript.exe");
        if (!Files.exists(cscript)) throw new FileNotFoundException(cscript.toString());
        ProcessBuilder pb = new ProcessBuilder(cscript.toString(), script.toString());

        runAndOutput(pb);
    }

    public static void runPs1(Path script) throws IOException, InterruptedException {
        // PowerShell is not version 1.0 but the folder name was never changed *shrug*
        Path powershell = Paths.get(System.getenv("WINDIR"), "system32", "WindowsPowerShell", "v1.0" , "powershell.exe");
        if (!Files.exists(powershell)) throw new FileNotFoundException(powershell.toString());
        // Should use prop "AllSigned" instead of ByPass
        ProcessBuilder pb = new ProcessBuilder(powershell.toString(), "-ExecutionPolicy",  "ByPass", "-File", script.toString());

        runAndOutput(pb);
    }

    public static void runPy2(Path script) throws IOException {
        String regPath = "SOFTWARE\\WOW6432Node\\Python\\PythonCore";
        String[] s = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, regPath);
        // Do some version stuff
        String location = (String)Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, regPath + "\\" + s[0] + "\\InstallPath", null);

        Path python = Paths.get(location , "python.exe");
        if (!Files.exists(python)) throw new FileNotFoundException(python.toString());
        ProcessBuilder pb = new ProcessBuilder(python.toString(), script.toString());

        runAndOutput(pb);
    }

    public static void runPy3(Path script) throws IOException {
        String regPath = "SOFTWARE\\Python\\PythonCore";
        String[] s = Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, regPath);
        // Do some version stuff
        String location = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, regPath + "\\" + s[0] + "\\InstallPath", "ExecutablePath");

        Path python = Paths.get(location);
        if (!Files.exists(python)) throw new FileNotFoundException(python.toString());
        ProcessBuilder pb = new ProcessBuilder(python.toString(), script.toString());

        runAndOutput(pb);
    }

    public static void runAndOutput(ProcessBuilder pb) throws IOException {
        System.out.println(pb.command());

        final Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while((line=br.readLine())!=null) sb.append(line);
        System.out.println("result: " + sb);
        sb = new StringBuilder();
        while((line=br2.readLine())!=null) sb.append(line);
        System.out.println("error: " + sb);
        p.destroy();
    }
}
