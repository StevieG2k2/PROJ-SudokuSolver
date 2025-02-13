import si.fri.algator.execute.AbstractAlgorithm;

public abstract class ProjectAbstractAlgorithm extends AbstractAlgorithm {

  protected abstract Output execute(Input sudokuSolverInput);
 
  @Override
  public TestCase getCurrentTestCase() {
    return (TestCase) super.getCurrentTestCase(); 
  }

  @Override
  public void run() {    
    algorithmOutput = execute(getCurrentTestCase().getInput());
  }
}