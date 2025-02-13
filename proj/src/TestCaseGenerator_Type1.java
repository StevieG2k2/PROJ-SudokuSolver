import si.fri.algator.entities.Variables;
import static si.fri.algator.execute.AbstractTestCase.PROPS;
import static si.fri.algator.execute.AbstractTestCase.TESTS_PATH;
import si.fri.algator.global.ErrorStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import si.fri.algator.execute.AbstractTestCaseGenerator;

/**
 * Type1 test case generator for project SudokuSolver
 *
 * @author ...
*/
public class TestCaseGenerator_Type1 extends AbstractTestCaseGenerator {    
  @Override
  public TestCase generateTestCase(Variables generatingParameters) {
    // read params from generatingParameters
    String path	= generatingParameters.getVariable(TESTS_PATH, "").getStringValue();
    String filename = generatingParameters.getVariable("Filename", "").getStringValue();
    int    N = generatingParameters.getVariable("N", 0).getIntValue();
	  String[] temp = new String[2];
    String testFile = path + File.separator + filename;

    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(testFile)));
      for(int i = 0; i < N; i++){
      	br.readLine();
      }
      if(br.ready()){
      	temp = br.readLine().split(" ");
      }
      br.close();
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Error reading file " + testFile);
      return null;
    }
    // create a testcase and set ...
    TestCase sudokuSolverTestCase = new TestCase();

    // ... the input ...
    // ...
    sudokuSolverTestCase.setInput(new Input(temp[0]));
    //System.out.println(temp[0]);

    // ... and the expected output.
    // ...
    // sudokuSolverTestCase.setExpectedOutput(new Output(...));


    // Add properties ...
    Variables testcaseParameters = new Variables(generatingParameters);
    testcaseParameters.addProperty(PROPS, "Puzzle", temp[0]);
    testcaseParameters.addProperty(PROPS, "Type", "Type1");
    testcaseParameters.addProperty(PROPS, "Filename", filename);
    testcaseParameters.addProperty(PROPS, "N", N);
    sudokuSolverTestCase.getInput().setParameters(testcaseParameters);

    // ... and return the testcase
    return sudokuSolverTestCase;
  }
}