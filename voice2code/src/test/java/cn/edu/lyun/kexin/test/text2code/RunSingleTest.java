package cn.edu.lyun.kexin.test.text2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class RunSingleTest {
  private static boolean isDebug = false;

  public static void main(String[] args) throws IOException {
    RunSingleTest.isDebug = true;
    String dir = System.getProperty("user.dir");
    Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
    String filePath = dir
        +
        "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/testcases/29Continue_3.voiceJava";
        // "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/all.voiceJava";
    // filePath =
    // "/Users/stefanzan/Research/2021/voice2CodeInVoiceJava/util/ListHelper.voiceJava";
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    int counter = 1;
    for (String line; (line = br.readLine()) != null;) {
      System.out.println(counter + ": " + line);
      counter++;
      // skip empty line
      if (line.equals("")) {
        continue;
      }
      if(counter == 17){
        System.out.println("stop for inspection");
      }
      text2CompilationUnit.generate(line);

      if (RunSingleTest.isDebug) {
        text2CompilationUnit.generatePNGofHoleAST();
      }
      if (!RunSingleTest.isDebug) {
        try {
          TimeUnit.SECONDS.sleep((long) 0.5);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    br.close();

    text2CompilationUnit.generatePNGofHoleAST();
    System.out.println("done");
  }
}
