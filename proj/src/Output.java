import si.fri.algator.execute.AbstractOutput;

/**
 * Output class for the project SudokuSolver.
 * 
 * @author ...
 */
public class Output extends AbstractOutput {

  
  public String result;
  
  public Output(String AlgOut) {       
    result = AlgOut;
  }
  
  @Override
  public String toString() {
    // TODO: provide a handy representation (include only important data) of this Output object
    // This method is used only in the debugging process.
    return super.toString();
  }
  
}