import si.fri.algator.execute.AbstractInput;

/**
 * Input class for the project SudokuSolver.
 * 
 * @author ...
 */
public class Input extends AbstractInput {

  
  public String sudoku;

  
  public Input(String sudoku) {   
    this.sudoku = sudoku;
  }
      
  @Override
  public String toString() {
    // TODO: provide a handy representation (include only important data) of the Input
    // This method is used only in the debugging process.
    return super.toString();
  }

}