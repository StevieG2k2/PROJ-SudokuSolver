import si.fri.algator.execute.Counters;
/**
 * Algorithm for project SudokuSolver.
 *
 * @author ...
 */
public class Algorithm_COUNT extends ProjectAbstractAlgorithm {
  public int GRID_SIZE = 9;

  // class celice
  class Cell {
    int value; // 0 prazna, 1-9 polna

    Cell(){
        value = 0;
    }

    boolean isSolved(){
        return value != 0;
    }

    void setValue(int value){
        this.value = value;
    }

    int getValue(){
        return value;
    }
  }

  // class 9x9 polja igre
  class Grid {
      Cell[][] grid;
      
      Grid(){
          // napolnis grid s celicami
          grid = new Cell[GRID_SIZE][];
          for(int i = 0; i < GRID_SIZE; i++){
              grid[i] = new Cell[GRID_SIZE];
              for(int j = 0; j < GRID_SIZE; j++){
                  grid[i][j] = new Cell();
              }
          }
      }

      Cell getCell(int row, int col){
          return grid[row][col];
      }
      
      void setCellValue(int row, int col, int value){
          grid[row][col].setValue(value);
      }
  }

  // nalaganje iz stringa v strukturo
  public void initializeGrid(Grid grid, String puzzle){
    if(puzzle.length() != 81){
        throw new IllegalArgumentException("Niz mora biti 81 znakov stevk dolg.");
    }
    /* loop po nizu in nalaganje nenicelnih stevk v strukturo */
    for(int i = 0; i < GRID_SIZE*GRID_SIZE; i++){
        int row = i / GRID_SIZE;
        int col = i % GRID_SIZE;
        // prazne celice 0 ali . (. => -1) 
        int value = Character.getNumericValue(puzzle.charAt(i));
        // != dela ce so prazne 0, > dela ce so prazne 0 al .
        if(value > 0){
            grid.setCellValue(row, col, value);
        }
    }
  }

  // rekurzivna funkcija, ki uporablja backtracking (brute force) za resitev sudokuja
  public boolean backtrack(Grid grid){
    Counters.addToCounter("CALL",  1);
    for(int r = 0; r < GRID_SIZE; r++){
        for(int c = 0; c < GRID_SIZE; c++){
            if(!grid.getCell(r, c).isSolved()){
                for(int k = 1; k < GRID_SIZE + 1; k++){
                    boolean possible = true;
                    for(int i = 0; i < GRID_SIZE; i++){
                        if(grid.getCell(r, i).isSolved() && grid.getCell(r, i).getValue() == k) possible = false;
                        if(grid.getCell(i, c).isSolved() && grid.getCell(i, c).getValue() == k) possible = false;
                        int br = 3 *(r / 3) + (i / 3);
                        int bc = 3 *(c / 3) + (i % 3);
                        if(grid.getCell(br, bc).isSolved() && grid.getCell(br, bc).getValue() == k) possible = false;
                    }
                    if(possible){
                        grid.getCell(r, c).setValue(k);
                        if(backtrack(grid)) return true;
                        grid.getCell(r, c).setValue(0);
                    }
                }
                return false;
            }
        }
    }
    return true;
  }

  @Override
  protected Output execute(Input input) {
    Grid grid = new Grid();
    initializeGrid(grid, input.sudoku);
    backtrack(grid);
    String niz = "";
    for(int i = 0; i < 9; i++){
      for(int j = 0; j < 9; j++){
        niz = niz +""+ (grid.getCell(i,j).getValue() == 0 ? "." : grid.getCell(i,j).getValue());
      }
    }
    Output output = new Output(niz);    
    return output;
  }

}

