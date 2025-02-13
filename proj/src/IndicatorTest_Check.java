import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import si.fri.algator.execute.AbstractIndicatorTest;

/**
 * IndicatorTest for "Check" indicator of SudokuSolver project.
 * 
 * @author ...
 */
public class IndicatorTest_Check extends AbstractIndicatorTest<TestCase, Output> {
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

  @Override
  public Object getValue(TestCase testCase, Output algorithmOutput) {
    Grid grid = new Grid();
    initializeGrid(grid, algorithmOutput.result);
    for(int i = 0; i < GRID_SIZE; i++){
      HashMap<Integer, List<Integer>> Vrstica = new HashMap<>();
      HashMap<Integer, List<Integer>> Stolpec = new HashMap<>();
      HashMap<Integer, List<Integer>> Box = new HashMap<>();
      for(int j = 0; j < GRID_SIZE; j++){
          // ce je katerakoli celica prazna, NI pravilno reseno 
          if(!grid.getCell(i, j).isSolved()) return 1;
          Vrstica.putIfAbsent(grid.getCell(i, j).getValue(), new ArrayList<>());
          Vrstica.get(grid.getCell(i, j).getValue()).add(j);
          if(!grid.getCell(j, i).isSolved()) return 1;
          Stolpec.putIfAbsent(grid.getCell(j, i).getValue(), new ArrayList<>());
          Stolpec.get(grid.getCell(j, i).getValue()).add(j);
          int br = 3 *(i / 3) + (j / 3);
          int bc = 3 *(i % 3) + (j % 3);
          if(!grid.getCell(br, bc).isSolved()) return 1;
          Box.putIfAbsent(grid.getCell(br, bc).getValue(), new ArrayList<>());
          Box.get(grid.getCell(br, bc).getValue()).add(j);
      }
      // ce vrstica/stolpec/box nima tocno 9 razlicnih stevil, NI pravilno reseno
      if(Vrstica.keySet().size() != GRID_SIZE || Stolpec.keySet().size() != GRID_SIZE || Box.keySet().size() != GRID_SIZE) return 1;
      for(int j = 1; j < GRID_SIZE + 1; j++){
          // ce se v vrstici/stolpcu/boxu katerokoli stevilo ne pojavi tocno 1x, NI pravilno reseno 
          if(Vrstica.get(j).size() != 1 || Stolpec.get(j).size() != 1 || Box.get(j).size() != 1) return 1;
      }
    }
    // ce gre skozi vse brez problemov je v grid-u pravilna resitev
    return 0;
  }
}
