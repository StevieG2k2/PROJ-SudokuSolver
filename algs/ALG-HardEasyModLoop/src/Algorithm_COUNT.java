import si.fri.algator.execute.Counters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Iterator;

/**
 * Algorithm for project SudokuSolver.
 *
 * @author ...
 */
public class Algorithm_COUNT extends ProjectAbstractAlgorithm {
  public final int GRID_SIZE = 9;

    // class celice
    class Cell {
        int value; // 0 prazna, 1-9 polna
        Set<Integer> candidates = new HashSet<>(); // kandidati
        int row;
        int column;
        boolean clue;

        Cell(){
            value = 0;
            clue = false;
            // na zacetki vsi kandidati
            for(int i = 1; i <= GRID_SIZE; i++){
                candidates.add(i);
            }
        }

        boolean isSolved(){
            return value != 0;
        }

        void setValue(int value){
            this.value = value;
            candidates.clear();
        }

        int getValue(){
            return value;
        }

        void removeCandidate(int candidate){
            candidates.remove(candidate);
        }

        Set<Integer> getCandidates(){
            return candidates;
        }
		
		void setCandidates(Set<Integer> candidateSet){
			candidates.clear();
			for(int k : candidateSet){
			    this.candidates.add(k);
			}
		}
        
        public int getRow() {
            return row;
        }
        public void setRow(int row) {
            this.row = row;
        }
        public int getColumn() {
            return column;
        }
        public void setColumn(int column) {
            this.column = column;
        }
        public boolean isClue(){
            return clue;
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
                    grid[i][j].setRow(i);
                    grid[i][j].setColumn(j);
                }
            }
        }

        Cell getCell(int row, int col){
            return grid[row][col];
        }
        
        void setCellValue(int row, int col, int value){
            grid[row][col].setValue(value);
        }

		void copyGrid(Grid gridToCopy){
			for(int i = 0; i < GRID_SIZE; i++){
				for(int j = 0; j < GRID_SIZE; j++){
					Cell cell = grid[i][j];
					Cell cellToCopy = gridToCopy.getCell(i,j);
					cell.setValue(cellToCopy.getValue());
					for(int k : cellToCopy.getCandidates()){
						cell.candidates.add(k);
					}
					cell.clue = cellToCopy.isClue();
				}
			}
		}

        int getCandidateCount(){
            int cc = 0;
            for(int i = 0; i < GRID_SIZE; i++){
                for(int j = 0; j < GRID_SIZE; j++){
                    cc += grid[i][j].getCandidates().size();
                }
            }
            return cc;
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
                grid.getCell(row, col).clue = true;
            }
        }
        /* odstrani neprimerne kandidate iz celic */
        eliminateCandidates(grid);
    }

    // osnovno odstranjevanje kandidatov po vrsticah/stolpcih in boxih
    public void eliminateCandidates(Grid grid){
        for(int row = 0; row < GRID_SIZE; row++){
            for(int col = 0; col < GRID_SIZE; col++){
                Cell cell = grid.getCell(row, col);
                if(cell.isSolved()) continue;
                for(int i = 0; i < GRID_SIZE; i++){
                    // row
                    cell.removeCandidate(grid.getCell(row, i).value);
                    // column
                    cell.removeCandidate(grid.getCell(i, col).value);
                    // box 3x3
                    int boxRow = 3 *(row / 3) + i / 3;
                    int boxCol = 3 *(col / 3) + i % 3;
                    cell.removeCandidate(grid.getCell(boxRow, boxCol).value);
                }
            }
        }
    }

    // rekurzivno polnjenje celic, ki imajo enega kandidata
    public void fillSingles(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("singles");
        boolean jeDodalo = false;
        for(int row = 0; row < GRID_SIZE; row++){
            for(int col = 0; col < GRID_SIZE; col++){
                Cell celica = grid.getCell(row, col);
                // ce celica ni resena in ima enga kandidata se ta vstavi v celico
                if(!celica.isSolved() && celica.candidates.size() == 1){
                    int ediniKandidat = celica.getCandidates().iterator().next();
                    celica.setValue(ediniKandidat);
                    jeDodalo = true;
                }
            }
        }
        eliminateCandidates(grid);
        if(jeDodalo){
            fillSingles(grid);
        }
    }

    // rekurzivno polnjenje celic, kjer je en st kandidat v vrstici/stolpcu/boxu
    public void fillHiddenSingles(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("hidden singles");
        boolean jeDodalo = false;
        for(int n = 0; n < GRID_SIZE; n++){
            // ROW
            // size 10 zato kr indeksiram z 1
            int[] countROW = new int[10];
            // prestej kandidate v vrstici
            for(int col = 0; col < GRID_SIZE; col++){
                Cell cell = grid.getCell(n, col);
                for(int kandidat : cell.getCandidates()){
                    countROW[kandidat]++;
                }
            }
            // pogleda kateri kandidat se pojavi 1-krat
            for(int k = 1; k <= GRID_SIZE; k++){
                if(countROW[k] == 1){
                    // polupa na njegovo mesto in ga doda
                    for(int col = 0; col < GRID_SIZE; col++){
                        Cell cell = grid.getCell(n, col);
                        if(cell.getCandidates().contains(k)){
                            // System.out.println(n+""+col);
                            cell.setValue(k);
                            eliminateCandidates(grid);
                            jeDodalo = true;
                            break;
                        }
                    }
                }
            }

            // COL
            int[] countCOL = new int[10];
            // prestej kandidate v stolpcu
            for(int row = 0; row < GRID_SIZE; row++){
                Cell cell = grid.getCell(row, n);
                for(int kandidat : cell.getCandidates()){
                    countCOL[kandidat]++;
                }
            }
            // pogleda kateri kandidat se pojavi 1-krat
            for(int k = 1; k <= GRID_SIZE; k++){
                if(countCOL[k] == 1){
                    // polupa na njegovo mesto in ga doda
                    for(int row = 0; row < GRID_SIZE; row++){
                        Cell cell = grid.getCell(row, n);
                        if(cell.getCandidates().contains(k)){
                            // System.out.println(row+""+n);
                            cell.setValue(k);
                            eliminateCandidates(grid);
                            jeDodalo = true;
                            break;
                        }
                    }
                }
            }

            // BOX
            int[] countBOX = new int[10];
            int boxRow = 3 * (n / 3);
            int boxCol = 3 * (n % 3);
            // prestej kandidate v boxu
            for(int row = 0; row < 3; row++){
                for(int col = 0; col < 3; col++){
                    Cell cell = grid.getCell(boxRow + row, boxCol + col);
                    for(int kandidat : cell.getCandidates()){
                        countBOX[kandidat]++;
                    }
                }
            }
            //pogleda kateri kandidat se pojavi 1-krat
            for(int k = 1; k <= GRID_SIZE; k++){
                if(countBOX[k] == 1){
                    // polupa na njegovo mesto in ga doda
                    for(int row = 0; row < 3; row++){
                        for(int col = 0; col < 3; col++){
                            Cell cell = grid.getCell(boxRow + row, boxCol + col);
                            if(cell.getCandidates().contains(k)){
                                // System.out.println((boxRow + row)+""+(boxCol + col));
                                cell.setValue(k);
                                eliminateCandidates(grid);
                                jeDodalo = true;
                                break;
                            }
                        }
                    }
                }
            }

        }
        if(jeDodalo){
            fillHiddenSingles(grid);
        }
    }

    // rekurzivno odstrani kandidate po vrsticah/stolpcih, na katere kazejo edini kandidati v boxu
    public void elimPointing(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("pointing");
        boolean jeRemovalo = false;
        // loop po boxih
        for(int boxRow = 0; boxRow < 3; boxRow++){
            for(int boxCol = 0; boxCol < 3; boxCol++){
                // loop po kandidatih
                for(int k = 1; k <= GRID_SIZE; k++){
                    boolean[]rowsB = new boolean[3];
                    boolean[]colsB = new boolean[3];
                    int rowCount = 0;
                    int colCount = 0;
                    // loop po posaneznem boxu
                    for(int r = 0; r < 3; r++){
                        for(int c = 0; c < 3; c++){
                            Cell cell = grid.getCell(3 * boxRow + r, 3 * boxCol + c);
                            // ce je kandidat v celici se oznaci true v vrstico in stolpec array
                            if(!cell.isSolved() && cell.getCandidates().contains(k)){
                                if(!rowsB[r]){
                                    rowsB[r] = true;
                                    rowCount++;
                                }
                                if(!colsB[c]){
                                    colsB[c] = true;
                                    colCount++;
                                }
                            }
                        }
                    }
                    // ce so vsi kandidati v boxu v eni vrstici, ostale v tej vrstici removi 
                    if(rowCount == 1){
                        for(int r = 0; r < 3; r++){
                            // poisci vrstico za brisanje
                            if(rowsB[r]){
                                for(int c = 0; c < 9; c++){
                                    Cell cell = grid.getCell(3 * boxRow + r, c);
                                    // preskoci gledani box
                                    if(c / 3 != boxCol && cell.getCandidates().contains(k)){
                                        cell.removeCandidate(k);
                                        jeRemovalo = true;
                                        // System.out.println(k+" ;"+(3 * boxRow + r)+","+c);
                                    }
                                }
                            }
                        }
                    }
                    // ce so vsi kandidati v boxu v enem stolpci, ostale v tem stolpci removi
                    if(colCount == 1){
                        for(int c = 0; c < 3; c++){
                            // poisci stolpec za brsianje
                            if(colsB[c]){
                                for(int r = 0; r < 9; r++){
                                    Cell cell = grid.getCell(r, 3 * boxCol + c);
                                    // preskoci gledani box
                                    if(r / 3 != boxRow && cell.getCandidates().contains(k)){
                                        cell.removeCandidate(k);
                                        jeRemovalo = true;
                                        // System.out.println(k+" C;"+r+","+(3 * boxCol + c));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(jeRemovalo){
            elimPointing(grid);
        }
    }
    
    // rekurzivno odstrani kandidate v boxu, kateri niso edini kandidati v vrstici
    public void elimBoxLineReduct(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("box line");
        boolean jeRemovalo = false;
        // loop po kandidatih
        for(int k = 1; k <= GRID_SIZE; k++){
            for(int i = 0; i < GRID_SIZE; i++){
                boolean[]rowsB = new boolean[3];
                boolean[]colsB = new boolean[3];
                // gleda vrstico in stolpec hkrati za kandidate 
                for(int j = 0; j < GRID_SIZE; j++){
                    
                    Cell cellR = grid.getCell(i,j);
                    Cell cellC = grid.getCell(j,i);
                    if(!cellR.isSolved() && cellR.getCandidates().contains(k)){
                        rowsB[j/3] = true;
                    }
                    if(!cellC.isSolved() && cellC.getCandidates().contains(k)){
                        colsB[j/3] = true;
                    }
                }
                // ce so kandidati v boxu edini v vrstici, ostale v tem boxu removi 
                if((rowsB[0] && !rowsB[1] && !rowsB[2]) || (!rowsB[0] && rowsB[1] && !rowsB[2]) || (!rowsB[0] && !rowsB[1] && rowsB[2])){
                    for(int c = 0; c < 3; c++){
                        if(rowsB[c]){
                            int r = i / 3;
                            for(int boxRow = 0; boxRow < 3; boxRow++){
                                for(int boxCol = 0; boxCol < 3; boxCol++){
                                    // poisce cell, ki ji mora removat in remova kandidata
                                    Cell cell = grid.getCell( 3 * r + boxRow, 3 * c + boxCol);
                                    if(!cell.isSolved() && cell.getCandidates().contains(k) && (3 * r + boxRow) != i){
                                        cell.removeCandidate(k);
                                        jeRemovalo = true;
                                        // System.out.println("brisuR "+k+", na:"+(3 * r + boxRow)+","+(3 * c + boxCol));
                                    }
                                }
                            }
                        }
                    }
                }

                // ce so kandidati v boxu edini v stolpci, ostale v tem boxu removi 
                if((colsB[0] && !colsB[1] && !colsB[2]) || (!colsB[0] && colsB[1] && !colsB[2]) || (!colsB[0] && !colsB[1] && colsB[2])){
                    for(int r = 0; r < 3; r++){
                        if(colsB[r]){
                            int c = i / 3;
                            for(int boxRow = 0; boxRow < 3; boxRow++){
                                for(int boxCol = 0; boxCol < 3; boxCol++){
                                    // poisce cell, ki ji mora removat in remova kandidata
                                    Cell cell = grid.getCell( 3 * r + boxRow, 3 * c + boxCol);
                                    if(!cell.isSolved() && cell.getCandidates().contains(k) && (3 * c + boxCol) != i){
                                        cell.removeCandidate(k);
                                        jeRemovalo = true;
                                        // System.out.println("brisuC "+k+", na:"+(3 * r + boxRow)+","+(3 * c + boxCol));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(jeRemovalo){
            elimBoxLineReduct(grid);
        }
    }


    // FUNKCIJE PAROV, TRIPLOV, QUADOV NISO REKURZIVNE ZARADI INIFNIT LOOPA
    // razn ce dam da gleda st najdenih P/T/Q

    // PAIRS
    // funkcija odstrani kandidate, v vrstici/stolpcu/boxu, enake kandidatom v paru vendar niso v celicah para
    public void elimNakedPairs(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("naked pairs");
        // boolean jeRemovalo = false;
        for(int i = 0; i < GRID_SIZE; i++){
            // VRSTICA
            // gledamo do 9-1 da ne brezveze gledamo zadnjega elementa
            for(int j1 = 0; j1 < GRID_SIZE - 1; j1++){
                Cell np1 = grid.getCell(i, j1);
                // ce celica ni resena in ima 2 kandidata
                if(!np1.isSolved() && np1.getCandidates().size() == 2){
                    // shrani kandidata
                    Set<Integer> pairCandidates = np1.getCandidates();
                    for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                        Cell np2 = grid.getCell(i, j2);
                        // ce druga celica ni resena in ima enake kandidate si dubu par
                        if(!np2.isSolved() && np2.getCandidates().equals(pairCandidates)){
                            // System.out.println("NPR "+i+" "+j1+" - "+i+" "+j2);
                            for(int k = 0; k < GRID_SIZE; k++){
                                // indeks vrstice je razlicen od teh, ki so del para
                                if(k != j1 && k != j2){
                                    Cell elimCell = grid.getCell(i, k);
                                    if (!elimCell.isSolved()){
                                        // odstrani kandidata iz polj, ki niso del para
                                        for (Integer kandidat : pairCandidates){
                                            elimCell.removeCandidate(kandidat);
                                        }
                                        // jeRemovalo = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // STOLPEC
            // gledamo do 9-1 da ne brezveze gledamo zadnjega elementa
            for(int j1 = 0; j1 < GRID_SIZE - 1; j1++){
                Cell np1 = grid.getCell(j1, i);
                // ce celica ni resena in ima 2 kandidata
                if(!np1.isSolved() && np1.getCandidates().size() == 2){
                    // shrani kandidata
                    Set<Integer> pairCandidates = np1.getCandidates();
                    for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                        Cell np2 = grid.getCell(j2, i);
                        // ce druga celica ni resena in ima enake kandidate si dubu par
                        if(!np2.isSolved() && np2.getCandidates().equals(pairCandidates)){
                            // System.out.println("NPC "+j1+" "+i+" - "+j2+" "+i);
                            for(int k = 0; k < GRID_SIZE; k++){
                                // indeks vrstice je razlicen od teh, ki so del para
                                if(k != j1 && k != j2){
                                    Cell elimCell = grid.getCell(k, i);
                                    if (!elimCell.isSolved()){
                                        // odstrani kandidata iz polj, ki niso del para
                                        for (Integer kandidat : pairCandidates){
                                            elimCell.removeCandidate(kandidat);
                                        }
                                        // jeRemovalo = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // BOX
            // box top left cell coords
            int startRow = 3 * (i / 3);
            int startCol = 3 * (i % 3);
            for(int j1 = 0; j1 < GRID_SIZE - 1; j1++){
                // izracun coord celice1
                int row1 = startRow + (j1 / 3);
                int col1 = startCol + (j1 % 3);
                Cell bp1 = grid.getCell(row1, col1);
                // ce celica ni resena in ima 2 kandidata
                if(!bp1.isSolved() && bp1.getCandidates().size() == 2){
                    // shrani kandidata
                    Set<Integer> pairBox = bp1.getCandidates();
                    for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                        // izracun coord celice2
                        int row2 = startRow + (j2 / 3);
                        int col2 = startCol + (j2 % 3);
                        Cell bp2 = grid.getCell(row2, col2);
                        // ce druga celica ni resena in ima enake kandidate si dubu par
                        if(!bp2.isSolved() && bp2.getCandidates().equals(pairBox)){
                            // System.out.println("NPB "+row1+" "+col1+" - "+row2+" "+col2);
                            for(int k = 0; k < GRID_SIZE; k++){
                                // izracun coord celice kjer se remova kandidate
                                int rowElimCell = startRow + (k / 3);
                                int colElimCell = startCol + (k % 3);
                                if(!((rowElimCell == row1 && colElimCell == col1) || (rowElimCell == row2 && colElimCell == col2))){
                                    Cell elimCell = grid.getCell(rowElimCell, colElimCell);
                                    if (!elimCell.isSolved()){
                                        // odstrani kandidata iz polj, ki niso del para
                                        for(int a = 1; a < GRID_SIZE + 1; a++){
                                            if(pairBox.contains(a)) elimCell.removeCandidate(a);
                                        }
                                        // jeRemovalo = true;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        // if(jeRemovalo){
        //     elimNakedPairs(grid);
        // }
    }

    // funkcija odstrani kandidate, v celicah para, ki niso del para
    public void elimHiddenPairs(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("hidden pairs");
        // boolean jeRemovalo = false;
        for(int i = 0; i < GRID_SIZE; i++){
            // VRSTICA
            Map<Integer, List<Integer>> candidateMap = new HashMap<>();
            // loop po vseh celicah v vrstici
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(i, j);
                if(!cell.isSolved()){
                    // doda zapis v map
                    for(int kandidat : cell.getCandidates()){
                        // kljuc == kandidat
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        // vrednost == pozicija kandidata (row, col)
                        candidateMap.get(kandidat).add(j);
                    }
                }
            }
            // loop za iskanje prve kandidatne celice
            for(int kandidat1 = 1; kandidat1 < GRID_SIZE; kandidat1++){
                List<Integer> positions1 = candidateMap.get(kandidat1);
                // ce celica ima kandidate (ni resena) in ima samo 2, je kandidat
                if(positions1 != null && positions1.size() == 2){
                    // loop za iskanje druge kandidatne celice
                    for(int kandidat2 = kandidat1 + 1; kandidat2 < GRID_SIZE + 1; kandidat2++){
                        List<Integer> positions2 = candidateMap.get(kandidat2);
                        // ce je kljuc razlicen, vsebina pa enaka, je kandidat
                        if (positions2 != null && positions2.equals(positions1)){
                            // shrani stevili kandidatov
                            Set<Integer> hiddenPair = new HashSet<>();
                            hiddenPair.add(kandidat1);
                            hiddenPair.add(kandidat2);
                            // System.out.println("parr "+hiddenPair);
                            for (int index : positions1){
                                Cell cell = grid.getCell(i, index);
                                // odstrani vse kandidate razen tistih dveh shranjenih
                                cell.getCandidates().retainAll(hiddenPair);
                            }
                        }
                    }
                }
            }
            
            // STOLPEC
            candidateMap = new HashMap<>();
            // loop po vseh celicah v stolpci
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(j, i);
                if(!cell.isSolved()){
                    // doda zapis v map
                    for(int kandidat : cell.getCandidates()){
                        // kljuc == kandidat
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        // vrednost == pozicija kandidata
                        candidateMap.get(kandidat).add(j);
                    }
                }
            }
            // loop za iskanje prve kandidatne celice
            for(int kandidat1 = 1; kandidat1 < GRID_SIZE; kandidat1++){
                List<Integer> positions1 = candidateMap.get(kandidat1);
                // ce celica ima kandidate (ni resena) in ima samo 2, je kandidat
                if(positions1 != null && positions1.size() == 2){
                    // loop za iskanje druge kandidatne celice
                    for(int kandidat2 = kandidat1 + 1; kandidat2 < GRID_SIZE + 1; kandidat2++){
                        List<Integer> positions2 = candidateMap.get(kandidat2);
                        // ce je kljuc razlicen, vsebina pa enaka, je kandidat
                        if (positions2 != null && positions2.equals(positions1)){
                            // shrani stevili kandidatov
                            Set<Integer> hiddenPair = new HashSet<>();
                            hiddenPair.add(kandidat1);
                            hiddenPair.add(kandidat2);
                            // System.out.println("parc "+hiddenPair);
                            for (int index : positions1){
                                Cell cell = grid.getCell(index, i);
                                // odstrani vse kandidate razen tistih dveh shranjenih
                                cell.getCandidates().retainAll(hiddenPair);
                            }
                        }
                    }
                }
            }

            // BOX
            int startRow = 3 * (i / 3), startCol = 3 * (i % 3);
            candidateMap = new HashMap<>();
            // loop po vseh celicah v boxi
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(startRow + (j / 3), startCol + (j % 3));
                if(!cell.isSolved()){
                    // doda zapis v map
                    for(int kandidat : cell.getCandidates()){
                        // kljuc == kandidat
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        // vrednost == pozicija kandidata
                        candidateMap.get(kandidat).add(j);
                    }
                }
            }
            // loop za iskanje prve kandidatne celice
            for(int kandidat1 = 1; kandidat1 < GRID_SIZE; kandidat1++){
                // position1/2 ima shranjeno zaporedno celico 0-8
                List<Integer> positions1 = candidateMap.get(kandidat1);
                // ce celica ima kandidate (ni resena) in ima samo 2, je kandidat
                if(positions1 != null && positions1.size() == 2){
                    // loop za iskanje druge kandidatne celice
                    for(int kandidat2 = kandidat1 + 1; kandidat2 < GRID_SIZE + 1; kandidat2++){
                        List<Integer> positions2 = candidateMap.get(kandidat2);
                        // ce je kljuc razlicen, vsebina pa enaka, je kandidat
                        if (positions2 != null && positions2.equals(positions1)){
                            Set<Integer> hiddenPair = new HashSet<>();
                            hiddenPair.add(kandidat1);
                            hiddenPair.add(kandidat2);
                            // System.out.println("par "+hiddenPair);
                            int br1 = startRow + (positions1.get(0) / 3);
                            int bc1 = startCol + (positions1.get(0) % 3);
                            int br2 = startRow + (positions1.get(1) / 3);
                            int bc2 = startCol + (positions1.get(1) % 3);
                            Cell cell = grid.getCell(br1, bc1);
                            // odstrani vse kandidate razen tistih dveh shranjenih
                            cell.getCandidates().retainAll(hiddenPair);
                            cell = grid.getCell(br2, bc2);
                            cell.getCandidates().retainAll(hiddenPair);
                        }
                    }
                }
            }
        }
        // if(jeRemovalo){
        //     elimHiddenPairs(grid);
        // }
    }

    // TRIPLES
    // funkcija odstrani kandidate, v vrstici/stolpcu/boxu, enake kandidatom v triplu vendar niso v celicah tripla
    public void elimNakedTriples(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("naked triples");
        for(int i = 0; i < GRID_SIZE; i++){
            // VRSTICA
            // loop po vseh unijah treh celic vrstice
            for(int j1 = 0; j1 < GRID_SIZE - 2; j1++){
                Cell rt1 = grid.getCell(i, j1);
                if(rt1.isSolved()) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 1; j2++){
                    Cell rt2 = grid.getCell(i, j2);
                    if(rt2.isSolved()) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE; j3++){
                        Cell rt3 = grid.getCell(i, j3);
                        if(rt3.isSolved()) continue;
                        Set<Integer> unijaKandidatov = new HashSet<>(rt1.getCandidates());
                        unijaKandidatov.addAll(rt2.getCandidates());
                        unijaKandidatov.addAll(rt3.getCandidates());
                        // ce je unija velika 3, si dubu tripl
                        if(unijaKandidatov.size() == 3){
                            // System.out.println("NTR "+unijaKandidatov);
                            for(int k = 0; k < GRID_SIZE; k++){
                                if (k != j1 && k != j2 && k != j3){
                                    Cell elimCell = grid.getCell(i, k);
                                    for (Integer kandidat : unijaKandidatov){
                                        elimCell.removeCandidate(kandidat);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // STOLPEC
            // loop po vseh unijah treh celic stolpca
            for(int j1 = 0; j1 < GRID_SIZE - 2; j1++){
                Cell ct1 = grid.getCell(j1, i);
                if(ct1.isSolved()) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 1; j2++){
                    Cell ct2 = grid.getCell(j2, i);
                    if(ct2.isSolved()) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE; j3++){
                        Cell ct3 = grid.getCell(j3, i);
                        if(ct3.isSolved()) continue;
                        Set<Integer> unijaKandidatov = new HashSet<>(ct1.getCandidates());
                        unijaKandidatov.addAll(ct2.getCandidates());
                        unijaKandidatov.addAll(ct3.getCandidates());
                        // System.out.println(unijaKandidatov);
                        // ce je unija velika 3, si dubu tripl
                        if(unijaKandidatov.size() == 3){
                            // System.out.println("NTC "+unijaKandidatov);
                            for(int k = 0; k < GRID_SIZE; k++){
                                if (k != j1 && k != j2 && k != j3){
                                    Cell elimCell = grid.getCell(k, i);
                                    for (Integer kandidat : unijaKandidatov){
                                        elimCell.removeCandidate(kandidat);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // BOX
            int startRow = 3 * (i / 3);
            int startCol = 3 * (i % 3);
            // loop po vseh unijah treh celic boxa
            for(int j1 = 0; j1 < GRID_SIZE - 2; j1++){
                int row1 = startRow + (j1 / 3);
                int col1 = startCol + (j1 % 3);
                Cell bt1 = grid.getCell(row1, col1);
                if(bt1.isSolved()) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 1; j2++){
                    int row2 = startRow + (j2 / 3);
                    int col2 = startCol + (j2 % 3);
                    Cell bt2 = grid.getCell(row2, col2);
                    if(bt2.isSolved()) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE; j3++){
                        int row3 = startRow + (j3 / 3);
                        int col3 = startCol + (j3 % 3);
                        Cell bt3 = grid.getCell(row3, col3);
                        if(bt3.isSolved()) continue;
                        Set<Integer> unijaKandidatov = new HashSet<>(bt1.getCandidates());
                        unijaKandidatov.addAll(bt2.getCandidates());
                        unijaKandidatov.addAll(bt3.getCandidates());
                        // ce je unija velika 3, si dubu tripl
                        if(unijaKandidatov.size() == 3){
                            // System.out.println("NTB "+unijaKandidatov);
                            for(int k = 0; k < GRID_SIZE; k++){
                                if (k != j1 && k != j2 && k != j3){
                                    int rowElim = startRow + (k / 3);
                                    int colElim = startCol + (k % 3);
                                    Cell elimCell = grid.getCell(rowElim, colElim);
                                    for (Integer kandidat : unijaKandidatov){
                                        elimCell.removeCandidate(kandidat);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidate, v celicah tripla, ki niso del tripla
    public void elimHiddenTriples(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("hidden triples");
        for(int i = 0; i < GRID_SIZE; i++){
            // VRSTICA
            Map<Integer, List<Integer>> candidateMap = new HashMap<>();
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(i, j);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(j);
                    }
                }

            }
            for(int j1 = 1; j1 < GRID_SIZE - 1; j1++){
                if(candidateMap.get(j1) == null || candidateMap.get(j1).size() > 3 || candidateMap.get(j1).size() == 1) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                    if(candidateMap.get(j2) == null || candidateMap.get(j2).size() > 3 || candidateMap.get(j2).size() == 1) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE + 1; j3++){
                        if(candidateMap.get(j3) == null || candidateMap.get(j3).size() > 3 || candidateMap.get(j3).size() == 1) continue;
                        Set<Integer> unijaPozicij = new HashSet<>(candidateMap.get(j1));
                        unijaPozicij.addAll(candidateMap.get(j2));
                        unijaPozicij.addAll(candidateMap.get(j3));
                        if(unijaPozicij.size() == 3){
                            // shranimo kandidate
                            Set<Integer> kandidatiSet = new HashSet<>();
                            kandidatiSet.add(j1);
                            kandidatiSet.add(j2);
                            kandidatiSet.add(j3);
                            // System.out.println("HTR "+kandidatiSet);
                            for(int col : unijaPozicij){
                                Cell elimCell = grid.getCell(i, col);
                                elimCell.getCandidates().retainAll(kandidatiSet);
                            }
                        }
                    }
                }
            }


            // STOLPEC
            candidateMap = new HashMap<>();
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(j, i);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(j);
                    }
                }

            }
            for(int j1 = 1; j1 < GRID_SIZE - 1; j1++){
                if(candidateMap.get(j1) == null || candidateMap.get(j1).size() > 3 || candidateMap.get(j1).size() == 1) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                    if(candidateMap.get(j2) == null || candidateMap.get(j2).size() > 3 || candidateMap.get(j2).size() == 1) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE + 1; j3++){
                        if(candidateMap.get(j3) == null || candidateMap.get(j3).size() > 3 || candidateMap.get(j3).size() == 1) continue;
                        Set<Integer> unijaPozicij = new HashSet<>(candidateMap.get(j1));
                        unijaPozicij.addAll(candidateMap.get(j2));
                        unijaPozicij.addAll(candidateMap.get(j3));
                        if(unijaPozicij.size() == 3){
                            // shranimo kandidate
                            Set<Integer> kandidatiSet = new HashSet<>();
                            kandidatiSet.add(j1);
                            kandidatiSet.add(j2);
                            kandidatiSet.add(j3);
                            // System.out.println("HTC "+kandidatiSet);
                            for(int row : unijaPozicij){
                                Cell elimCell = grid.getCell(row, i);
                                elimCell.getCandidates().retainAll(kandidatiSet);
                            }
                        }
                    }
                }
            }

            // BOX
            int startRow = 3 * (i / 3);
            int startCol = 3 * (i % 3);
            candidateMap = new HashMap<>();
            for(int j = 0; j < GRID_SIZE; j++){
                int row = startRow + (j / 3);
                int col = startCol + (j % 3);
                Cell cell = grid.getCell(row, col);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(j);
                    }
                }

            }
            for(int j1 = 1; j1 < GRID_SIZE - 1; j1++){
                if(candidateMap.get(j1) == null || candidateMap.get(j1).size() > 3 || candidateMap.get(j1).size() == 1) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                    if(candidateMap.get(j2) == null || candidateMap.get(j2).size() > 3 || candidateMap.get(j2).size() == 1) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE + 1; j3++){
                        if(candidateMap.get(j3) == null || candidateMap.get(j3).size() > 3 || candidateMap.get(j3).size() == 1) continue;
                        Set<Integer> unijaPozicij = new HashSet<>(candidateMap.get(j1));
                        unijaPozicij.addAll(candidateMap.get(j2));
                        unijaPozicij.addAll(candidateMap.get(j3));
                        if(unijaPozicij.size() == 3){
                            // shranimo kandidate
                            Set<Integer> kandidatiSet = new HashSet<>();
                            kandidatiSet.add(j1);
                            kandidatiSet.add(j2);
                            kandidatiSet.add(j3);
                            // System.out.println("HTB "+kandidatiSet);
                            for(int n : unijaPozicij){
                                int row = startRow + (n / 3);
                                int col = startCol + (n % 3);
                                Cell elimCell = grid.getCell(row, col);
                                elimCell.getCandidates().retainAll(kandidatiSet);
                            }
                        }
                    }
                }
            }

        }
    }

    // QUADS
    // funkcija odstrani kandidate, v vrstici/stolpcu/boxu, enake kandidatom v quadu vendar niso v celicah quada
    public void elimNakedQuads(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("naked quads");
        for(int i = 0; i < GRID_SIZE; i++){
            // ROW
            // loop po vseh unijah stirih celic vrstice
            for(int j1 = 0; j1 < GRID_SIZE - 3; j1++){
                Cell rq1 = grid.getCell(i, j1);
                if(rq1.isSolved()) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 2; j2++){
                    Cell rq2 = grid.getCell(i, j2);
                    if(rq2.isSolved()) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE - 1; j3++){
                        Cell rq3 = grid.getCell(i, j3);
                        if(rq3.isSolved()) continue;
                        for(int j4 = j3 + 1; j4 < GRID_SIZE; j4++){
                            Cell rq4 = grid.getCell(i, j4);
                            if(rq4.isSolved()) continue;
                            Set<Integer> unijaKandidatov = new HashSet<>(rq1.getCandidates());
                            unijaKandidatov.addAll(rq2.getCandidates());
                            unijaKandidatov.addAll(rq3.getCandidates());
                            unijaKandidatov.addAll(rq4.getCandidates());
                            // ce je unija kandidatov velika 4, si dubu quad
                            if(unijaKandidatov.size() == 4){
                                // System.out.println("NQR "+unijaKandidatov);
                                for(int k = 0; k < GRID_SIZE; k++){
                                    if (k != j1 && k != j2 && k != j3 && k != j4){
                                        Cell elimCell = grid.getCell(i, k);
                                        for (Integer kandidat : unijaKandidatov){
                                            elimCell.removeCandidate(kandidat);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // COL
            // loop po vseh unijah stirih celic stolpca
            for(int j1 = 0; j1 < GRID_SIZE - 3; j1++){
                Cell cq1 = grid.getCell(j1, i);
                if(cq1.isSolved()) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 2; j2++){
                    Cell cq2 = grid.getCell(j2, i);
                    if(cq2.isSolved()) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE - 1; j3++){
                        Cell cq3 = grid.getCell(j3, i);
                        if(cq3.isSolved()) continue;
                        for(int j4 = j3 + 1; j4 < GRID_SIZE; j4++){
                            Cell cq4 = grid.getCell(j4, i);
                            if(cq4.isSolved()) continue;
                            Set<Integer> unijaKandidatov = new HashSet<>(cq1.getCandidates());
                            unijaKandidatov.addAll(cq2.getCandidates());
                            unijaKandidatov.addAll(cq3.getCandidates());
                            unijaKandidatov.addAll(cq4.getCandidates());
                            // ce je unija kandidatov velika 4, si dubu quad
                            if(unijaKandidatov.size() == 4){
                                // System.out.println("NQC "+unijaKandidatov);
                                for(int k = 0; k < GRID_SIZE; k++){
                                    if (k != j1 && k != j2 && k != j3 && k != j4){
                                        Cell elimCell = grid.getCell(k, i);
                                        for (Integer kandidat : unijaKandidatov){
                                            elimCell.removeCandidate(kandidat);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BOX
            int startRow = 3 * (i / 3);
            int startCol = 3 * (i % 3);
            // loop po vseh unijah stirih celic boxa
            for(int j1 = 0; j1 < GRID_SIZE - 3; j1++){
                int row1 = startRow + (j1 / 3);
                int col1 = startCol + (j1 % 3);
                Cell bq1 = grid.getCell(row1, col1);
                if(bq1.isSolved()) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 2; j2++){
                    int row2 = startRow + (j2 / 3);
                    int col2 = startCol + (j2 % 3);
                    Cell bq2 = grid.getCell(row2, col2);
                    if(bq2.isSolved()) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE - 1; j3++){
                        int row3 = startRow + (j3 / 3);
                        int col3 = startCol + (j3 % 3);
                        Cell bq3 = grid.getCell(row3, col3);
                        if(bq3.isSolved()) continue;
                        for(int j4 = j3 + 1; j4 < GRID_SIZE; j4++){
                            int row4 = startRow + (j4 / 3);
                            int col4 = startCol + (j4 % 3);
                            Cell bq4 = grid.getCell(row4, col4);
                            if(bq4.isSolved()) continue;
                            Set<Integer> unijaKandidatov = new HashSet<>(bq1.getCandidates());
                            unijaKandidatov.addAll(bq2.getCandidates());
                            unijaKandidatov.addAll(bq3.getCandidates());
                            unijaKandidatov.addAll(bq4.getCandidates());
                            // ce je unija velika 4, si dubu quad
                            if(unijaKandidatov.size() == 4){
                                // System.out.println("NQB "+unijaKandidatov);
                                for(int k = 0; k < GRID_SIZE; k++){
                                    if (k != j1 && k != j2 && k != j3 && k != j4){
                                        int rowElim = startRow + (k / 3);
                                        int colElim = startCol + (k % 3);
                                        Cell elimCell = grid.getCell(rowElim, colElim);
                                        for (Integer kandidat : unijaKandidatov){
                                            elimCell.removeCandidate(kandidat);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidate, v celicah quada, ki niso del quada
    public void elimHiddenQuads(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("hidden quads");
        for(int i = 0; i < GRID_SIZE; i++){
            // VRSTICA
            Map<Integer, List<Integer>> candidateMap = new HashMap<>();
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(i, j);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(j);
                    }
                }
            }
            for(int j1 = 1; j1 < GRID_SIZE - 2; j1++){
                if(candidateMap.get(j1) == null || candidateMap.get(j1).size() > 4 || candidateMap.get(j1).size() == 1) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE - 1; j2++){
                    if(candidateMap.get(j2) == null || candidateMap.get(j2).size() > 4 || candidateMap.get(j2).size() == 1) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE; j3++){
                        if(candidateMap.get(j3) == null || candidateMap.get(j3).size() > 4 || candidateMap.get(j3).size() == 1) continue;
                        for(int j4 = j3 + 1; j4 < GRID_SIZE + 1; j4++){
                            if(candidateMap.get(j4) == null || candidateMap.get(j4).size() > 4 || candidateMap.get(j4).size() == 1) continue;
                            Set<Integer> unijaPozicij = new HashSet<>(candidateMap.get(j1));
                            unijaPozicij.addAll(candidateMap.get(j2));
                            unijaPozicij.addAll(candidateMap.get(j3));
                            unijaPozicij.addAll(candidateMap.get(j4));
                            if(unijaPozicij.size() == 4){
                                // shranimo kandidate
                                Set<Integer> kandidatiSet = new HashSet<>();
                                kandidatiSet.add(j1);
                                kandidatiSet.add(j2);
                                kandidatiSet.add(j3);
                                kandidatiSet.add(j4);
                                // System.out.println("HQR "+kandidatiSet);
                                for(int col : unijaPozicij){
                                    Cell elimCell = grid.getCell(i, col);
                                    elimCell.getCandidates().retainAll(kandidatiSet);
                                }
                            }
                        }
                    }
                }
            }


            // STOLPEC
            candidateMap = new HashMap<>();
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(j, i);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(j);
                    }
                }

            }
            for(int j1 = 1; j1 < GRID_SIZE - 1; j1++){
                if(candidateMap.get(j1) == null || candidateMap.get(j1).size() > 3 || candidateMap.get(j1).size() == 1) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                    if(candidateMap.get(j2) == null || candidateMap.get(j2).size() > 3 || candidateMap.get(j2).size() == 1) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE + 1; j3++){
                        if(candidateMap.get(j3) == null || candidateMap.get(j3).size() > 3 || candidateMap.get(j3).size() == 1) continue;
                        Set<Integer> unijaPozicij = new HashSet<>(candidateMap.get(j1));
                        unijaPozicij.addAll(candidateMap.get(j2));
                        unijaPozicij.addAll(candidateMap.get(j3));
                        if(unijaPozicij.size() == 3){
                            // shranimo kandidate
                            Set<Integer> kandidatiSet = new HashSet<>();
                            kandidatiSet.add(j1);
                            kandidatiSet.add(j2);
                            kandidatiSet.add(j3);
                            // System.out.println("HTC "+kandidatiSet);
                            for(int row : unijaPozicij){
                                Cell elimCell = grid.getCell(row, i);
                                elimCell.getCandidates().retainAll(kandidatiSet);
                            }
                        }
                    }
                }
            }

            // BOX
            int startRow = 3 * (i / 3);
            int startCol = 3 * (i % 3);
            candidateMap = new HashMap<>();
            for(int j = 0; j < GRID_SIZE; j++){
                int row = startRow + (j / 3);
                int col = startCol + (j % 3);
                Cell cell = grid.getCell(row, col);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(j);
                    }
                }

            }
            for(int j1 = 1; j1 < GRID_SIZE - 1; j1++){
                if(candidateMap.get(j1) == null || candidateMap.get(j1).size() > 3 || candidateMap.get(j1).size() == 1) continue;
                for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                    if(candidateMap.get(j2) == null || candidateMap.get(j2).size() > 3 || candidateMap.get(j2).size() == 1) continue;
                    for(int j3 = j2 + 1; j3 < GRID_SIZE + 1; j3++){
                        if(candidateMap.get(j3) == null || candidateMap.get(j3).size() > 3 || candidateMap.get(j3).size() == 1) continue;
                        Set<Integer> unijaPozicij = new HashSet<>(candidateMap.get(j1));
                        unijaPozicij.addAll(candidateMap.get(j2));
                        unijaPozicij.addAll(candidateMap.get(j3));
                        if(unijaPozicij.size() == 3){
                            // shranimo kandidate
                            Set<Integer> kandidatiSet = new HashSet<>();
                            kandidatiSet.add(j1);
                            kandidatiSet.add(j2);
                            kandidatiSet.add(j3);
                            // System.out.println("HTB "+kandidatiSet);
                            for(int n : unijaPozicij){
                                int row = startRow + (n / 3);
                                int col = startCol + (n % 3);
                                Cell elimCell = grid.getCell(row, col);
                                elimCell.getCandidates().retainAll(kandidatiSet);
                            }
                        }
                    }
                }
            }

        }
    }


    // WINGS
    // funkcija odstrani kandidate, v vrsticah/stolpcih, razen v celicah, ki so v X-u
    public void elimXWing(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("X-Wing");
        // gleda VRSTICE, odstrani STOLPCE
        for(int i1 = 0; i1 < GRID_SIZE - 1; i1++){
            Map<Integer, List<Integer>> candidateMapR1 = new HashMap<>();
            // loop za pounit map1
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(i1, j);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMapR1.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMapR1.get(kandidat).add(j);
                    }
                }
            }
            for(int i2 = i1 +1; i2 < GRID_SIZE; i2++){
                Map<Integer, List<Integer>> candidateMapR2 = new HashMap<>();
                // loop za pounit map2
                for(int j = 0; j < GRID_SIZE; j++){
                    Cell cell = grid.getCell(i2, j);
                    if(!cell.isSolved()){
                        for(int kandidat : cell.getCandidates()){
                            candidateMapR2.putIfAbsent(kandidat, new ArrayList<>());
                            candidateMapR2.get(kandidat).add(j);
                        }
                    }
                }
                // loop za pogledat kateri kandidat naredi X-Wing
                for(int k = 1; k < GRID_SIZE + 1; k++){
                    List<Integer> positions1 = candidateMapR1.get(k);
                    List<Integer> positions2 = candidateMapR2.get(k);
                    // samo ce so obe listi polni pojdi naprej
                    if(!(positions1 != null && positions2 != null))continue;
                    // ce so kandidati samo na dveh pozicijah in so te enake = X-Wing
                    if(positions1.size() == 2 && positions2.size() == 2 && positions1.equals(positions2)){
                        // System.out.println("X-W: rmC"+positions1+" "+k);
                        for(int pozicija : positions1){
                            for(int n = 0; n < GRID_SIZE; n++){
                                // preskoce celice v X-Wingu
                                if(!(n != i1 && n != i2))continue;
                                Cell elimCell = grid.getCell(n, pozicija);
                                elimCell.removeCandidate(k);
                            }
                        }
                    }
                }
            }
        }
        // gleda STOLPCE, odstrani VRSTICE
        for(int i1 = 0; i1 < GRID_SIZE - 1; i1++){
            Map<Integer, List<Integer>> candidateMapR1 = new HashMap<>();
            // loop za pounit map1
            for(int j = 0; j < GRID_SIZE; j++){
                Cell cell = grid.getCell(j, i1);
                if(!cell.isSolved()){
                    for(int kandidat : cell.getCandidates()){
                        candidateMapR1.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMapR1.get(kandidat).add(j);
                    }
                }
            }
            for(int i2 = i1 +1; i2 < GRID_SIZE; i2++){
                Map<Integer, List<Integer>> candidateMapR2 = new HashMap<>();
                // loop za pounit map2
                for(int j = 0; j < GRID_SIZE; j++){
                    Cell cell = grid.getCell(j, i2);
                    if(!cell.isSolved()){
                        for(int kandidat : cell.getCandidates()){
                            candidateMapR2.putIfAbsent(kandidat, new ArrayList<>());
                            candidateMapR2.get(kandidat).add(j);
                        }
                    }
                }
                // loop za pogledat kateri kandidat naredi X-Wing
                for(int k = 1; k < GRID_SIZE + 1; k++){
                    List<Integer> positions1 = candidateMapR1.get(k);
                    List<Integer> positions2 = candidateMapR2.get(k);
                    // samo ce so obe listi polni pojdi naprej
                    if(!(positions1 != null && positions2 != null))continue;
                    // ce so kandidati samo na dveh pozicijah in so te enake = X-Wing
                    if(positions1.size() == 2 && positions2.size() == 2 && positions1.equals(positions2)){
                        // System.out.println("X-W: rmR"+positions1+" "+k);
                        for(int pozicija : positions1){
                            for(int n = 0; n < GRID_SIZE; n++){
                                // preskoce celice v X-Wingu
                                if(!(n != i1 && n != i2))continue;
                                Cell elimCell = grid.getCell(pozicija, n);
                                elimCell.removeCandidate(k);
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidate, v celici ce gre za RC, v vrsticah/stolpcih ce gre za BR/BC 
    public void elimYWing(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Y-Wing");
        for(int row = 0; row < GRID_SIZE; row++){
            for(int col = 0; col < GRID_SIZE; col++){
                if(!grid.getCell(row, col).isSolved() && grid.getCell(row, col).getCandidates().size() == 2){
                    Integer[] temp = new Integer[2];
                    grid.getCell(row, col).getCandidates().toArray(temp);
                    int k1 = (int)temp[0], k2 = (int)temp[1];
                    for(int i = 0; i < GRID_SIZE; i++){
                        // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata
                        if(i != col && i/3 != col/3 && !grid.getCell(row, i).isSolved() && grid.getCell(row, i).getCandidates().size() == 2 && !(grid.getCell(row, i).getCandidates().contains(k1) == grid.getCell(row, i).getCandidates().contains(k2))){
                            Integer[] temp2 = new Integer[2];
                            grid.getCell(row, i).getCandidates().toArray(temp2);
                            int k3 = (temp2[0] == k1 || temp2[0] == k2)? temp2[1] : temp2[0];
                            int k4 = (temp2[0] == k1 || temp2[0] == k2)? temp2[0] : temp2[1];
                            for(int j = 0; j < GRID_SIZE; j++){
                                // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata in ima k3 kandidata
                                if(j != row && j/3 != row/3 && !grid.getCell(j, col).isSolved() && grid.getCell(j, col).getCandidates().size() == 2 && !(grid.getCell(j, col).getCandidates().contains(k1) == grid.getCell(j, col).getCandidates().contains(k2)) && grid.getCell(j, col).getCandidates().contains(k3) && !grid.getCell(j, col).getCandidates().contains(k4)){
                                    // System.out.println("YRC "+row+" "+col+", "+j+" "+col+", "+row+" "+i);
                                    //zato kr gledas vrsrtico in stolpec od pivota, lohk removas k3 kar v celici diagonalno od pivota
                                    grid.getCell(j, i).removeCandidate(k3);
                                }
                            }
                        }
                        int boxR = 3 * (row / 3) + (i / 3);
                        int boxC = 3 * (col / 3) + (i % 3);
                        // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata
                        if((boxR != row || boxC != col) && !grid.getCell(boxR, boxC).isSolved() && grid.getCell(boxR, boxC).getCandidates().size() == 2 && !(grid.getCell(boxR, boxC).getCandidates().contains(k1) == grid.getCell(boxR, boxC).getCandidates().contains(k2))){
                            Integer[] temp2 = new Integer[2];
                            grid.getCell(boxR, boxC).getCandidates().toArray(temp2);
                            for(int j = 0; j < GRID_SIZE; j++){
                                int k3 = (temp2[0] == k1 || temp2[0] == k2)? temp2[1] : temp2[0];
                                int k4 = (temp2[0] == k1 || temp2[0] == k2)? temp2[0] : temp2[1];
                                // STOLPEC
                                // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata in ima k3 kandidata
                                if(boxC != col && j != row && j/3 != row/3 && !grid.getCell(j, col).isSolved() && grid.getCell(j, col).getCandidates().size() == 2 && !(grid.getCell(j, col).getCandidates().contains(k1) == grid.getCell(j, col).getCandidates().contains(k2)) && grid.getCell(j, col).getCandidates().contains(k3) && !grid.getCell(j, col).getCandidates().contains(k4)){
                                    // System.out.println("YBC "+row+" "+col+", "+boxR+" "+boxC+", "+j+" "+col);
                                    for(int n = 0; n < 3; n++){
                                        grid.getCell(3 * (j / 3) + n, boxC).removeCandidate(k3); //stolpec
                                        if(3 * (row / 3) + n != row) grid.getCell(3 * (row / 3) + n, col).removeCandidate(k3); //box
                                    }
                                }
                                // VRSTICA
                                // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata in ima k3 kandidata
                                if(boxR != row && j != col && j/3 != col/3 && !grid.getCell(row, j).isSolved() && grid.getCell(row, j).getCandidates().size() == 2 && !(grid.getCell(row, j).getCandidates().contains(k1) == grid.getCell(row, j).getCandidates().contains(k2)) && grid.getCell(row, j).getCandidates().contains(k3) && !grid.getCell(row, j).getCandidates().contains(k4)){
                                    // System.out.println("YBR "+row+" "+col+", "+boxR+" "+boxC+", "+row+" "+j);
                                    for(int n = 0; n < 3; n++){
                                        grid.getCell(boxR, 3 * (j / 3) + n).removeCandidate(k3); //vrstica
                                        if(3 * (col / 3) + n != col) grid.getCell(row, 3 * (col / 3) + n).removeCandidate(k3); //box
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidate, v vrsticah/stolpcih, razen v celicah, ki so v swordfishu (3x3 X-Wing)
    public void elimSwordfish(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Swordfish");
        // loop po kandidatih
        for(int k = 1; k < GRID_SIZE + 1; k++){
            // loop za prve 2-3 celice
            for(int c1 = 0; c1 < GRID_SIZE - 2; c1++){
                // loop za druge 2-3 celice
                for(int c2 = c1 + 1; c2 < GRID_SIZE - 1; c2++){
                    // loop za zadnje 2-3 celice
                    for(int c3 = c2 + 1; c3 < GRID_SIZE; c3++){
                        Map<Integer, List<Integer>> mapaPozicijR = new HashMap<>();
                        Map<Integer, List<Integer>> mapaPozicijC = new HashMap<>();
                        // loop za napolnit mapo
                        for(int i = 0; i < GRID_SIZE; i++){
                            // ce ima celica v prvi VRSTICI kandidata
                            if(grid.getCell(c1, i).getCandidates().contains(k)){
                                // kljuc == st 0
                                mapaPozicijR.putIfAbsent(0, new ArrayList<>());
                                // vrednost == pozicija v vrstici
                                mapaPozicijR.get(0).add(i);
                            }
                            if(grid.getCell(c2, i).getCandidates().contains(k)){
                                mapaPozicijR.putIfAbsent(1, new ArrayList<>());
                                mapaPozicijR.get(1).add(i);
                            }
                            if(grid.getCell(c3, i).getCandidates().contains(k)){
                                mapaPozicijR.putIfAbsent(2, new ArrayList<>());
                                mapaPozicijR.get(2).add(i);
                            }
                            // ce ima celica v prvem STOLPCI kandidata
                            if(grid.getCell(i, c1).getCandidates().contains(k)){
                                // kljuc == st 0
                                mapaPozicijC.putIfAbsent(0, new ArrayList<>());
                                // vrednost == pozicija v vrstici
                                mapaPozicijC.get(0).add(i);
                            }
                            if(grid.getCell(i, c2).getCandidates().contains(k)){
                                mapaPozicijC.putIfAbsent(1, new ArrayList<>());
                                mapaPozicijC.get(1).add(i);
                            }
                            if(grid.getCell(i, c3).getCandidates().contains(k)){
                                mapaPozicijC.putIfAbsent(2, new ArrayList<>());
                                mapaPozicijC.get(2).add(i);
                            }
                        }
                        if((mapaPozicijR.get(0) != null && mapaPozicijR.get(1) != null && mapaPozicijR.get(2) != null) && (mapaPozicijR.get(0).size() == 2 || mapaPozicijR.get(0).size() == 3) && (mapaPozicijR.get(1).size() == 2 || mapaPozicijR.get(1).size() == 3) && (mapaPozicijR.get(2).size() == 2 || mapaPozicijR.get(2).size() == 3)){
                            Set<Integer> unijaPozicij = new HashSet<>(mapaPozicijR.get(0));
                            unijaPozicij.addAll(mapaPozicijR.get(1));
                            unijaPozicij.addAll(mapaPozicijR.get(2));
                            if(unijaPozicij.size() == 3){
                                // System.out.println("SFR "+k+", "+c1+" "+c2+" "+c3);
                                for(int i = 0; i < GRID_SIZE; i++){
                                    if(i != c1 && i != c2 && i != c3){
                                        for(Integer stolpec : unijaPozicij)
                                            grid.getCell(i, stolpec).removeCandidate(k);
                                    }
                                }
                            }
                        }
                        if((mapaPozicijC.get(0) != null && mapaPozicijC.get(1) != null && mapaPozicijC.get(2) != null) && (mapaPozicijC.get(0).size() == 2 || mapaPozicijC.get(0).size() == 3) && (mapaPozicijC.get(1).size() == 2 || mapaPozicijC.get(1).size() == 3) && (mapaPozicijC.get(2).size() == 2 || mapaPozicijC.get(2).size() == 3)){
                            Set<Integer> unijaPozicij = new HashSet<>(mapaPozicijC.get(0));
                            unijaPozicij.addAll(mapaPozicijC.get(1));
                            unijaPozicij.addAll(mapaPozicijC.get(2));
                            if(unijaPozicij.size() == 3){
                                // System.out.println("SFC "+k+", "+c1+" "+c2+" "+c3);
                                for(int i = 0; i < GRID_SIZE; i++){
                                    if(i != c1 && i != c2 && i != c3){
                                        for(Integer vrstica : unijaPozicij)
                                            grid.getCell(vrstica, i).removeCandidate(k);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidata, v celici, kjer, ce bi bil vstavljen, bi preprecil vstavitev tega kandidata v sosednji box
    public void elimRectangle(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Rectangle Elim");
        for(int k = 1; k < GRID_SIZE + 1; k++){
            for(int row = 0; row < GRID_SIZE; row++){
                for(int col = 0; col < GRID_SIZE; col++){
                    if(!grid.getCell(row, col).getCandidates().contains(k)) continue;
                    // kljuc 0 == vrstica, 1 == stolpec
                    Map<Integer, List<Integer>> mapaKandidatov = new HashMap<>();
                    // loop za napounit mapo
                    for(int i = 0; i < GRID_SIZE; i++){
                        // VRSTICA
                        if(i != col && grid.getCell(row, i).getCandidates().contains(k)){
                            mapaKandidatov.putIfAbsent(0, new ArrayList<>());
                            mapaKandidatov.get(0).add(i);
                        }
                        // STOLPEC
                        if(i != row && grid.getCell(i, col).getCandidates().contains(k)){
                            mapaKandidatov.putIfAbsent(1, new ArrayList<>());
                            mapaKandidatov.get(1).add(i);
                        }
                    }
                    if(!mapaKandidatov.containsKey(0) || !mapaKandidatov.containsKey(1)) continue;
                    // remova se izbrano celico iz stolpca
                    if(mapaKandidatov.get(0).size() == 1 && mapaKandidatov.get(1).size() > 1){
                        if(mapaKandidatov.get(0).get(0)/3 == col/3) continue;
                        int c1 = mapaKandidatov.get(0).get(0);
                        for(Integer r1 : mapaKandidatov.get(1)){
                            if(r1/3 == row/3) continue;
                            Map<Integer, List<Integer>> mapaBoxa = new HashMap<>();
                            boolean kVnesen = false;
                            for(int i = 0; i < GRID_SIZE; i++){
                                if(grid.getCell(3 * (r1/3) + (i/3), 3 * (c1/3) + (i%3)).getCandidates().contains(k) && 3 * (r1/3) + (i/3) != r1 && 3 * (c1/3) + (i%3) != c1){
                                    mapaBoxa.putIfAbsent(0, new ArrayList<>());
                                    mapaBoxa.get(0).add(i);
                                }
                                if(grid.getCell(3 * (r1/3) + (i/3), 3 * (c1/3) + (i%3)).isSolved() && grid.getCell(3 * (r1/3) + (i/3), 3 * (c1/3) + (i%3)).getValue() == k) kVnesen = true;
                            }
                            if(kVnesen) continue;
                            if(mapaBoxa.get(0) == null){
                                // System.out.println("RectC "+k+" "+row+" "+col+", "+r1+" "+col);
                                grid.getCell(r1, col).removeCandidate(k);
                            }
                        }
                    }
                    // remova se izbrano celico iz vstice
                    if(mapaKandidatov.get(1).size() == 1 && mapaKandidatov.get(0).size() > 1){
                        if(mapaKandidatov.get(1).get(0)/3 == col/3) continue;
                        int r1 = mapaKandidatov.get(1).get(0);
                        for(Integer c1 : mapaKandidatov.get(0)){
                            if(c1/3 == row/3) continue;
                            Map<Integer, List<Integer>> mapaBoxa = new HashMap<>();
                            boolean kVnesen = false;
                            for(int i = 0; i < GRID_SIZE; i++){
                                if(grid.getCell(3 * (r1/3) + (i/3), 3 * (c1/3) + (i%3)).getCandidates().contains(k) && 3 * (r1/3) + (i/3) != r1 && 3 * (c1/3) + (i%3) != c1){
                                    mapaBoxa.putIfAbsent(0, new ArrayList<>());
                                    mapaBoxa.get(0).add(i);
                                }
                                if(grid.getCell(3 * (r1/3) + (i/3), 3 * (c1/3) + (i%3)).isSolved() && grid.getCell(3 * (r1/3) + (i/3), 3 * (c1/3) + (i%3)).getValue() == k) kVnesen = true;
                            }
                            if(kVnesen) continue;
                            if(mapaBoxa.get(0) == null){
                                // System.out.println("RectR "+k+" "+row+" "+col+", "+row+" "+c1);
                                grid.getCell(row, c1).removeCandidate(k);
                            }
                        }
                    }
                }
            }
        }
    }

    // podobno kot Y-Wing, samo da ima tudi pivot kandidata, ki ga brisemo samo v boxu kjer je pivot
    public void elimXYZWing(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("XYZ-Wing");
        for(int row = 0; row < GRID_SIZE; row++){
            for(int col = 0; col < GRID_SIZE; col++){
                if(!grid.getCell(row, col).isSolved() && grid.getCell(row, col).getCandidates().size() == 3){
                    Integer[] temp = new Integer[3];
                    grid.getCell(row, col).getCandidates().toArray(temp);
                    int k = (int)temp[0], k1 = (int)temp[1], k2 = (int)temp[2];
                    for(int i = 0; i < GRID_SIZE; i++){
                        int boxR = 3 * (row / 3) + (i / 3);
                        int boxC = 3 * (col / 3) + (i % 3);
                        // gre skozi samo ce je prava PRAZNA celica in ima 2 enakega kandidata, edn je tisti k se ga bo removalo
                        if((boxR != row || boxC != col) && !grid.getCell(boxR, boxC).isSolved() && grid.getCell(boxR, boxC).getCandidates().size() == 2 && ((grid.getCell(boxR, boxC).getCandidates().contains(k) && grid.getCell(boxR, boxC).getCandidates().contains(k1) && !grid.getCell(boxR, boxC).getCandidates().contains(k2)) || (grid.getCell(boxR, boxC).getCandidates().contains(k) && !grid.getCell(boxR, boxC).getCandidates().contains(k1) && grid.getCell(boxR, boxC).getCandidates().contains(k2)) || (!grid.getCell(boxR, boxC).getCandidates().contains(k) && grid.getCell(boxR, boxC).getCandidates().contains(k1) && grid.getCell(boxR, boxC).getCandidates().contains(k2)))){
                            Integer[] temp2 = new Integer[2], temp3 = new Integer[2];
                            for(int j = 0; j < GRID_SIZE; j++){
                                // STOLPEC
                                // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata in ima k3 kandidata
                                if(j != row && j/3 != row/3 && !grid.getCell(j, col).isSolved() && grid.getCell(j, col).getCandidates().size() == 2 && ((grid.getCell(j, col).getCandidates().contains(k) && grid.getCell(j, col).getCandidates().contains(k1) && !grid.getCell(j, col).getCandidates().contains(k2)) || (grid.getCell(j, col).getCandidates().contains(k) && !grid.getCell(j, col).getCandidates().contains(k1) && grid.getCell(j, col).getCandidates().contains(k2)) || (!grid.getCell(j, col).getCandidates().contains(k) && grid.getCell(j, col).getCandidates().contains(k1) && grid.getCell(j, col).getCandidates().contains(k2)))){
                                    grid.getCell(boxR, boxC).getCandidates().toArray(temp2);
                                    grid.getCell(j, col).getCandidates().toArray(temp3);
                                    // kandidati v celici v boxu
                                    Set<Integer> set1 = new HashSet<>();
                                    set1.add(temp2[0]);
                                    set1.add(temp2[1]);
                                    // kandidati v celici v vrstici
                                    Set<Integer> set2 = new HashSet<>();
                                    set2.add(temp3[0]);
                                    set2.add(temp3[1]);
                                    // celici v boxu in v vrstici morata imeti samo eno skupno spremenljiuko
                                    set1.retainAll(set2);
                                    if(set1.size() != 1) continue;
                                    int elimK = (int)set1.toArray()[0];
                                    // katero stevilo je enako v obeh celicah
                                    // System.out.println("XYZ_BC "+row+" "+col+", "+boxR+" "+boxC+", "+j+" "+col);
                                    for(int n = 0; n < 3; n++){
                                        if(3 * (row / 3) + n != row) grid.getCell(3 * (row / 3) + n, col).removeCandidate(elimK); //box
                                    }
                                }
                                // VRSTICA
                                // gre skozi samo ce je prava PRAZNA celica in ima samo enega enakega kandidata in ima k3 kandidata
                                if(j != col && j/3 != col/3 && !grid.getCell(row, j).isSolved() && grid.getCell(row, j).getCandidates().size() == 2 && ((grid.getCell(row, j).getCandidates().contains(k) && grid.getCell(row, j).getCandidates().contains(k1) && !grid.getCell(row, j).getCandidates().contains(k2)) || (grid.getCell(row, j).getCandidates().contains(k) && !grid.getCell(row, j).getCandidates().contains(k1) && grid.getCell(row, j).getCandidates().contains(k2)) || (!grid.getCell(row, j).getCandidates().contains(k) && grid.getCell(row, j).getCandidates().contains(k1) && grid.getCell(row, j).getCandidates().contains(k2)))){
                                    grid.getCell(boxR, boxC).getCandidates().toArray(temp2);
                                    grid.getCell(row, j).getCandidates().toArray(temp3);
                                    // kandidati v celici v boxu
                                    Set<Integer> set1 = new HashSet<>();
                                    set1.add(temp2[0]);
                                    set1.add(temp2[1]);
                                    // kandidati v celici v stolpcu
                                    Set<Integer> set2 = new HashSet<>();
                                    set2.add(temp3[0]);
                                    set2.add(temp3[1]);
                                    // celici v boxu in v stolpcu morata imeti samo eno skupno spremenljiuko
                                    set1.retainAll(set2);
                                    if(set1.size() != 1) continue;
                                    int elimK = (int)set1.toArray()[0];
                                    // katero stevilo je enako v obeh celicah
                                    // System.out.println("XYZ_BR "+row+" "+col+", "+boxR+" "+boxC+", "+row+" "+j);
                                    for(int n = 0; n < 3; n++){
                                        if(3 * (col / 3) + n != col) grid.getCell(row, 3 * (col / 3) + n).removeCandidate(elimK); //box
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija, ki resi edino prazno celico s 3 kandidati, ce imajo vse ostale celice 2 kandidata, stevilo praznih celic pa je liho
    public void elimBUG(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("BUG");
        int triCandidateCells = 0, emptyCells = 0;
        int tR = 0, tC = 0;
        // presteje prazne celice in celice s 3 kandidati
        for(int r = 0; r < GRID_SIZE; r++){
            for(int c = 0; c < GRID_SIZE; c++){
                if(!grid.getCell(r, c).isSolved()) emptyCells++;
                if(grid.getCell(r, c).getCandidates().size() > 3) return;
                if(grid.getCell(r, c).getCandidates().size() < 2) return;
                if(grid.getCell(r, c).getCandidates().size() == 3){
                    triCandidateCells++;
                    // mora bit samo ena celica s 3 kandidati
                    if(triCandidateCells > 1) return;
                    tR = r;
                    tC = c;
                } 
            }
        }
        // stevilo praznih celic mora bit liho in mora bit samo ena celica s 3 kandidati
        if(emptyCells % 2 != 0 && triCandidateCells == 1){
            Map<Integer, List<Integer>> candidateMap = new HashMap<>();
            // loop za napolnit mapo
            for(int i = 0; i < GRID_SIZE; i++){
                if(!grid.getCell(tR, i).isSolved()){
                    for(int kandidat : grid.getCell(tR, i).getCandidates()){
                        candidateMap.putIfAbsent(kandidat, new ArrayList<>());
                        candidateMap.get(kandidat).add(i);
                    }
                }
            }
            for(int k : candidateMap.keySet()){
                if(candidateMap.get(k).size() == 3){
                    // System.out.println("Bug "+k+", "+tR+" "+tC);
                    grid.getCell(tR, tC).setValue(k);
                    eliminateCandidates(grid);
                    return;
                }
            }
        }
    }

    // funkcija odstrani kandidate, ki vidijo obe "barvi" chaina ali vse kandidate barve, ki se dvakrat pojavi v row/col/box
    public void elimSinglesChain(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Singles Chain");
        for(int k = 1; k < GRID_SIZE + 1; k++){
            for(int r = 0; r < GRID_SIZE; r++){
                for(int c = 0; c < GRID_SIZE; c++){
                    Cell cell = grid.getCell(r, c);
                    // kljuc = celica, vrednost = "barva" 0,1
                    Map<Cell, Integer> mapaBarv = new HashMap<>();
                    // gre naprej samo ce je celica prazna, ima kandidata in se NI del nekega chaina
                    if(!cell.isSolved() && cell.getCandidates().contains(k) && !mapaBarv.containsKey(grid.getCell(r, c))){
                        // ce je chain valid se uporabi pravila singles chains
                        if(chainOK(grid, cell, k, mapaBarv, 0)){
                            // System.out.println(mapaBarv);
                            applyChain(grid, k, mapaBarv);
                        }//else{System.out.println("ni OK");}
                    }
                }
            }
        }
    }
    // funkcija naredi in pogleda ali je chain pravilen, ce je pravilen vrne true, cene false
    public boolean chainOK(Grid grid, Cell startCell, int k, Map<Cell, Integer> mapaBarv, int barva){
        Queue<Cell> vrsta = new LinkedList<>();
        vrsta.add(startCell);
        mapaBarv.put(startCell, 0);
        int nextBarva;
        // kljuci: 0 = row, 1 = col, 2 = box
        Map<Integer, List<Integer>> mapaPozicij = new HashMap<>();
        // loop dokler so mozne naslednje celice, vhodne celice
        while(!vrsta.isEmpty()){
            mapaPozicij.put(0, new ArrayList<>());
            mapaPozicij.put(1, new ArrayList<>());
            mapaPozicij.put(2, new ArrayList<>());
            Cell cell = vrsta.poll();
            nextBarva = mapaBarv.get(cell) == 0 ? 1 : 0;
            List<Cell> links = new ArrayList<>();
            int r = cell.getRow();
            int c = cell.getColumn();
            // loop za napounit mapo, da vidis kolk je kandidatov v row/col/box
            for(int i = 0; i < GRID_SIZE; i++){
                // ROW
                if(i != c && !grid.getCell(r, i).isSolved() && grid.getCell(r, i).getCandidates().contains(k)){
                    mapaPozicij.get(0).add(i);
                } 
                // COL
                if(i != r && !grid.getCell(i, c).isSolved() && grid.getCell(i, c).getCandidates().contains(k)){
                    mapaPozicij.get(1).add(i);
                }
                // BOX
                int boxR = 3 * (r / 3) + (i / 3);
                int boxC = 3 * (c / 3) + (i % 3);
                if((boxR != r || boxC != c) && !grid.getCell(boxR, boxC).isSolved() && grid.getCell(boxR, boxC).getCandidates().contains(k)){
                    mapaPozicij.get(2).add(i);
                }
            }
            // ce je kandidat samo edn (skipnli smo celico, za katero gledamo "sosede"), dodaj celico v vrsto 
            if(mapaPozicij.get(0) != null ? mapaPozicij.get(0).size() == 1 && !links.contains(grid.getCell(r, mapaPozicij.get(0).get(0))) : false){
                links.add(grid.getCell(r, mapaPozicij.get(0).get(0)));
            }
            if(mapaPozicij.get(1) != null ? mapaPozicij.get(1).size() == 1 && !links.contains(grid.getCell(mapaPozicij.get(1).get(0), c)) : false){
                links.add(grid.getCell(mapaPozicij.get(1).get(0), c));
            }
            if(mapaPozicij.get(2) != null ? mapaPozicij.get(2).size() == 1 && !links.contains(grid.getCell(3 * (r / 3) + (mapaPozicij.get(2).get(0) / 3), 3 * (c / 3) + (mapaPozicij.get(2).get(0) % 3))) : false){
                links.add(grid.getCell(3 * (r / 3) + (mapaPozicij.get(2).get(0) / 3), 3 * (c / 3) + (mapaPozicij.get(2).get(0) % 3)));
            }
            // loop po mogocih naslednjih celicah
            for(Cell nextCell : links){
                // se pogleda ce ima naslednja celica ze doloceno barvo
                if(mapaBarv.containsKey(nextCell)){
                    // ce ima naslednja celica enako barvo kot trenutna celica, je napacn chain
                    if(mapaBarv.get(nextCell).equals(mapaBarv.get(cell))) return false;
                }else{
                    // ce naslednja celica se nima dolocene barve, se to naredi
                    mapaBarv.put(nextCell, nextBarva);
                    // se oznace da je celica del nekega chaina in se jo doda v vrsto, da se pogleda mogoce njene naslednje linke chaina
                    vrsta.add(nextCell);
                }
            }
            if(vrsta.isEmpty() && cell == startCell){
                // System.out.println("cant connect cell");
                mapaBarv.remove(cell);
                return false;
            }
        }
        // System.out.println("res je OK");
        return true;
    }
    // funkcija izvrsi odstranitev kandidatov v celicah na podlagi mapeBarv
    public void applyChain(Grid grid, int k, Map<Cell, Integer> mapaBarv){
        // System.out.println("apply "+k);
        // ce so v row/col/box 2 celici enake barve, je ta barva napacna, torej je druga barva pravilna in lahko v vse celice druge barve vstavimo kandidata 
        for (Map.Entry<Cell, Integer> entry1 : mapaBarv.entrySet()) {
            Cell cell1 = entry1.getKey();
            int color1 = entry1.getValue();
            for (Map.Entry<Cell, Integer> entry2 : mapaBarv.entrySet()) {
                Cell cell2 = entry2.getKey();
                int color2 = entry2.getValue();
                if (color1 == color2 && cell1 != cell2 && (cell1.getRow() == cell2.getRow() || cell1.getColumn() == cell2.getColumn() || (cell1.getRow() / 3 == cell2.getRow() / 3 && cell1.getColumn() / 3 == cell2.getColumn() / 3))) {
                    for (Map.Entry<Cell, Integer> entry : mapaBarv.entrySet()) {
                        Cell cell = entry.getKey();
                        int color = entry.getValue();
                        if (color == color1) {
                            // System.out.println("rm color "+color+" "+cell.getRow()+cell.getColumn());
                            cell.removeCandidate(k);
                        }
                    }
                    return;
                }
            }
        }
        // remova celico, ki vide celice obeh barv
        for (Map.Entry<Cell, Integer> entry1 : mapaBarv.entrySet()) {
            Cell cell1 = entry1.getKey();
            int color1 = entry1.getValue();
            for (Map.Entry<Cell, Integer> entry2 : mapaBarv.entrySet()) {
                Cell cell2 = entry2.getKey();
                int color2 = entry2.getValue();
                if(color1 != color2 && cell1 != cell2 && cell1.getRow() != cell2.getRow() && cell1.getColumn() != cell2.getColumn()){
                    if(grid.getCell(cell1.getRow(), cell2.getColumn()).getCandidates().contains(k) && !mapaBarv.containsKey(grid.getCell(cell1.getRow(), cell2.getColumn()))){
                        // System.out.println("RM obe barve "+cell1.getRow()+cell2.getColumn()+" "+cell1.getRow()+cell1.getColumn()+" "+cell2.getRow()+cell2.getColumn());
                        grid.getCell(cell1.getRow(), cell2.getColumn()).removeCandidate(k);
                    }
                    if(grid.getCell(cell2.getRow(), cell1.getColumn()).getCandidates().contains(k) && !!mapaBarv.containsKey(grid.getCell(cell2.getRow(), cell1.getColumn()))){
                        // System.out.println("rm obe barve "+cell2.getRow()+cell1.getColumn());
                        grid.getCell(cell2.getRow(), cell1.getColumn()).removeCandidate(k);
                    }
                }
            }
        }
    }

    // funkcija remova kandidate da se izognemo smtronosnemu pravokotniku (predpostavimo da ima sudoku samo 1 resitev)
    public void elimAvoidableRect(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Avoidable Rect");
        // loop za dobit izpolnjeno celico, ki ni podana na zacetki
        for(int r = 0; r < GRID_SIZE; r++){
            for(int c = 0; c < GRID_SIZE; c++){
                if(!grid.getCell(r, c).isClue() && grid.getCell(r, c).isSolved()){
                    // loop za dobit ostali dve izpolnjeni celici, ki niso podani na zacetku
                    for(int r1 = 0; r1 < GRID_SIZE; r1++){
                        for(int c1 = 0; c1 < GRID_SIZE; c1++){
                            // row in col nista enaka, dve celici morata met enako vrednost
                            if((r1 != r && c1 != c) && grid.getCell(r1, c).isSolved() && !grid.getCell(r1, c).isClue() && grid.getCell(r, c1).isSolved() && !grid.getCell(r, c1).isClue() && grid.getCell(r1, c).getValue() == grid.getCell(r, c1).getValue() && !grid.getCell(r1, c1).isSolved()){
                                // System.out.println("3cell RM "+r+c+" "+r1+c+" "+r+c1);
                                // odstrani kandidata, ki se nahaja v prvi dobljeni celici
                                grid.getCell(r1, c1).removeCandidate(grid.getCell(r, c).getValue());
                            }
                        }
                    }
                    // loop za dobit drugo izpolnjeno celico in pogledat ali bi lahko prislo do smrtonosnega pravokotnika
                    for(int i = 0; i < GRID_SIZE; i++){
                        // VRSTICA
                        if(i != c && !grid.getCell(r, i).isClue() && grid.getCell(r, i).isSolved()){
                            for(int j = 0; j < GRID_SIZE; j++){
                                if(j != r && !grid.getCell(j, c).isSolved() && grid.getCell(j, c).getCandidates().size() == 2 && grid.getCell(j, c).getCandidates().contains(grid.getCell(r, i).getValue()) && !grid.getCell(j, i).isSolved() && grid.getCell(j, i).getCandidates().size() == 2 && grid.getCell(j, i).getCandidates().contains(grid.getCell(r, c).getValue())){
                                    Set<Integer> set1 = new HashSet<>();
                                    Set<Integer> set2 = new HashSet<>();
                                    set1.addAll(grid.getCell(j, c).getCandidates());
                                    set1.remove(grid.getCell(r, i).getValue());
                                    set2.addAll(grid.getCell(j, i).getCandidates());
                                    set2.remove(grid.getCell(r, c).getValue());
                                    if(set1.iterator().next() == set2.iterator().next()){
                                        // System.out.println("RMR "+r+c+" "+r+i+" "+j+c+" "+j+i);
                                        for(int l = 0; l < GRID_SIZE; l++){
                                            if(l != c && l != i){
                                                grid.getCell(j, l).removeCandidate(set1.iterator().next());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // STOLPEC
                        if(i != r && !grid.getCell(i, c).isClue() && grid.getCell(i, c).isSolved()){
                            for(int j = 0; j < GRID_SIZE; j++){
                                if(j != c && !grid.getCell(r, j).isSolved() && grid.getCell(r, j).getCandidates().size() == 2 && grid.getCell(r, j).getCandidates().contains(grid.getCell(i, c).getValue()) && !grid.getCell(i, j).isSolved() && grid.getCell(i, j).getCandidates().size() == 2 && grid.getCell(i, j).getCandidates().contains(grid.getCell(r, c).getValue())){
                                    Set<Integer> set1 = new HashSet<>();
                                    Set<Integer> set2 = new HashSet<>();
                                    set1.addAll(grid.getCell(r, j).getCandidates());
                                    set1.remove(grid.getCell(i, c).getValue());
                                    set2.addAll(grid.getCell(i, j).getCandidates());
                                    set2.remove(grid.getCell(r, c).getValue());
                                    if(set1.iterator().next() == set2.iterator().next()){
                                        // System.out.println("RMC "+r+c+" "+i+c+" "+r+j+" "+i+j);
                                        for(int l = 0; l < GRID_SIZE; l++){
                                            if(l != r && l != i){
                                                grid.getCell(l, j).removeCandidate(set1.iterator().next());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidate, v vrsticah/stolpcih, razen v celicah, ki so v jellyfishu (4x4 Swordfish,X-Wing)
    public void elimJellyfish(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Jellyfish");
        for(int k = 1; k < GRID_SIZE + 1; k++){
            // loopi za dobit po 4 celice na vrstoico/stolpec
            for(int c1 = 0; c1 < GRID_SIZE - 3; c1++){
                for(int c2 = c1 + 1; c2 < GRID_SIZE - 2; c2++){
                    for(int c3 = c2 + 1; c3 < GRID_SIZE - 1; c3++){
                        for(int c4 = c3 + 1; c4 < GRID_SIZE; c4++){
                            Map<Integer, List<Integer>> mapaPozicijR = new HashMap<>();
                            Map<Integer, List<Integer>> mapaPozicijC = new HashMap<>();
                            // loop za napolnit mapo
                            for(int i = 0; i < GRID_SIZE; i++){
                                // shrane pozicije VRSTICE v mapo
                                if(grid.getCell(c1, i).getCandidates().contains(k)){
                                    // kljuc == 0
                                    mapaPozicijR.putIfAbsent(0, new ArrayList<>());
                                    // vrednost == pozicija v vrstici
                                    mapaPozicijR.get(0).add(i);
                                }
                                if(grid.getCell(c2, i).getCandidates().contains(k)){
                                    mapaPozicijR.putIfAbsent(1, new ArrayList<>());
                                    mapaPozicijR.get(1).add(i);
                                }
                                if(grid.getCell(c3, i).getCandidates().contains(k)){
                                    mapaPozicijR.putIfAbsent(2, new ArrayList<>());
                                    mapaPozicijR.get(2).add(i);
                                }
                                if(grid.getCell(c4, i).getCandidates().contains(k)){
                                    mapaPozicijR.putIfAbsent(3, new ArrayList<>());
                                    mapaPozicijR.get(3).add(i);
                                }
                                // shrane pozicije STOLPCA v mapo
                                if(grid.getCell(i, c1).getCandidates().contains(k)){
                                    // kljuc == 0
                                    mapaPozicijC.putIfAbsent(0, new ArrayList<>());
                                    // vrednost == pozicija v vrstici
                                    mapaPozicijC.get(0).add(i);
                                }
                                if(grid.getCell(i, c2).getCandidates().contains(k)){
                                    mapaPozicijC.putIfAbsent(1, new ArrayList<>());
                                    mapaPozicijC.get(1).add(i);
                                }
                                if(grid.getCell(i, c3).getCandidates().contains(k)){
                                    mapaPozicijC.putIfAbsent(2, new ArrayList<>());
                                    mapaPozicijC.get(2).add(i);
                                }
                                if(grid.getCell(i, c4).getCandidates().contains(k)){
                                    mapaPozicijC.putIfAbsent(3, new ArrayList<>());
                                    mapaPozicijC.get(3).add(i);
                                }
                            }
                            if((mapaPozicijR.get(0) != null && mapaPozicijR.get(1) != null && mapaPozicijR.get(2) != null && mapaPozicijR.get(3) != null) && (mapaPozicijR.get(0).size() >= 2 && mapaPozicijR.get(0).size() <= 4) && (mapaPozicijR.get(1).size() >= 2 && mapaPozicijR.get(1).size() <= 4) && (mapaPozicijR.get(2).size() >= 2 && mapaPozicijR.get(2).size() <= 4) && (mapaPozicijR.get(3).size() >= 2 && mapaPozicijR.get(3).size() <= 4)){
                                Set<Integer> unijaPozicij = new HashSet<>(mapaPozicijR.get(0));
                                unijaPozicij.addAll(mapaPozicijR.get(1));
                                unijaPozicij.addAll(mapaPozicijR.get(2));
                                unijaPozicij.addAll(mapaPozicijR.get(3));
                                if(unijaPozicij.size() == 4){
                                    // System.out.println("JFR "+k+", "+c1+" "+c2+" "+c3+" "+c4);
                                    for(int i = 0; i < GRID_SIZE; i++){
                                        if(i != c1 && i != c2 && i != c3 && i != c4){
                                            for(Integer stolpec : unijaPozicij)
                                                grid.getCell(i, stolpec).removeCandidate(k);
                                        }
                                    }
                                }
                            }
                            if((mapaPozicijC.get(0) != null && mapaPozicijC.get(1) != null && mapaPozicijC.get(2) != null && mapaPozicijC.get(3) != null) && (mapaPozicijC.get(0).size() >= 2 && mapaPozicijC.get(0).size() <= 4) && (mapaPozicijC.get(1).size() >= 2 && mapaPozicijC.get(1).size() <= 4) && (mapaPozicijC.get(2).size() >= 2 && mapaPozicijC.get(2).size() <= 4) && (mapaPozicijC.get(3).size() >= 2 && mapaPozicijC.get(3).size() <= 4)){
                                Set<Integer> unijaPozicij = new HashSet<>(mapaPozicijC.get(0));
                                unijaPozicij.addAll(mapaPozicijC.get(1));
                                unijaPozicij.addAll(mapaPozicijC.get(2));
                                unijaPozicij.addAll(mapaPozicijC.get(3));
                                if(unijaPozicij.size() == 4){
                                    // System.out.println("JFC "+k+", "+c1+" "+c2+" "+c3+" "+c4);
                                    for(int i = 0; i < GRID_SIZE; i++){
                                        if(i != c1 && i != c2 && i != c3 && i != c4){
                                            for(Integer vrstica : unijaPozicij)
                                                grid.getCell(vrstica, i).removeCandidate(k);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // odstrani kandidate tako da ne pride do srtonosnega pravokotnika
    public void elimUniqRect(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("Uniq Rectangles");
        for(int i = 0; i < GRID_SIZE; i++){
            // VRSTICA
            // gledamo do 9-1 da ne brezveze gledamo zadnjega elementa
            for(int j1 = 0; j1 < GRID_SIZE - 1; j1++){
                Cell np1 = grid.getCell(i, j1);
                // ce celica ni resena in ima 2 kandidata
                if(!np1.isSolved() && np1.getCandidates().size() == 2){
                    // shrani kandidata
                    Set<Integer> pairCandidates = np1.getCandidates();
                    for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                        Cell np2 = grid.getCell(i, j2);
                        // ce druga celica ni resena in ima enake kandidate si dubu par
                        if(!np2.isSolved() && np2.getCandidates().equals(pairCandidates)){
                            // TYPE 1
                            Integer[] temp = new Integer[2];
                            np1.getCandidates().toArray(temp);
                            for(int l = 0; l < GRID_SIZE; l++){
                                // gre skozi ce dobi se en par po stolpci in ce ima cetrta celica vsaj enega od kandidatov
                                if(l != i && grid.getCell(l, j1).getCandidates().equals(np1.getCandidates()) && grid.getCell(l, j2).getCandidates().size() >= 2 && (grid.getCell(l, j2).getCandidates().contains(temp[0]) || grid.getCell(l, j2).getCandidates().contains(temp[1]))){
                                    // System.out.println("R1 "+i+j1+" "+i+j2+" "+l+j2+" "+temp[0]+temp[1]);
                                    grid.getCell(l, j2).removeCandidate(temp[0]);
                                    grid.getCell(l, j2).removeCandidate(temp[1]);
                                }
                                // isto kot zgornje, samo da sta celica drugega para in celica, ki mora imeti vsaj enega kandidata zamenjani
                                if(l != i && grid.getCell(l, j2).getCandidates().equals(np1.getCandidates()) && grid.getCell(l, j1).getCandidates().size() >= 2 && (grid.getCell(l, j1).getCandidates().contains(temp[0]) || grid.getCell(l, j1).getCandidates().contains(temp[1]))){
                                    // System.out.println("R1 "+i+j1+" "+i+j2+" "+l+j1+" "+temp[0]+temp[1]);
                                    grid.getCell(l, j1).removeCandidate(temp[0]);
                                    grid.getCell(l, j1).removeCandidate(temp[1]);
                                }
                            }
                            // TYPE 2
                            Integer[] temp2 = new Integer[2];
                            np1.getCandidates().toArray(temp2);
                            for(int l = 0; l < GRID_SIZE; l++){
                                // gre skozi ce dobi dve enaki celici v isti vrsti, ki imata oba kandidata para in se skupnega tretjega 
                                if(l != i && grid.getCell(l, j1).getCandidates().size() == 3 && grid.getCell(l, j1).getCandidates().contains(temp2[0]) && grid.getCell(l, j1).getCandidates().contains(temp2[1]) && grid.getCell(l, j1).getCandidates().equals(grid.getCell(l, j2).getCandidates())){
                                    // System.out.println("R2 "+i+j1+" "+i+j2+" "+l);
                                    for(int m = 0; m < GRID_SIZE; m++){
                                        for(Integer tretjiK : grid.getCell(l, j1).getCandidates()){
                                            if(tretjiK == temp2[0] || tretjiK == temp2[1]) continue;
                                            // odstrani tretjega kandidata v tej vrstici, razen v prej dobljenih celicah
                                            if(m != j1 && m != j2) grid.getCell(l, m).removeCandidate(tretjiK);
                                            int boxR = 3 * (l / 3) + (m / 3);
                                            int boxC = 3 * (j1 / 3) + (m % 3);
                                            // ce sta dobljeni celici v istem boxu, remova kandidata se v boxu
                                            if(j1 / 3 == j2 / 3 && !(boxR == l && (boxC == j1 || boxC == j2))){
                                                grid.getCell(boxR, boxC).removeCandidate(tretjiK);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // STOLPEC
            // gledamo do 9-1 da ne brezveze gledamo zadnjega elementa
            for(int j1 = 0; j1 < GRID_SIZE - 1; j1++){
                Cell np1 = grid.getCell(j1, i);
                // ce celica ni resena in ima 2 kandidata
                if(!np1.isSolved() && np1.getCandidates().size() == 2){
                    // shrani kandidata
                    Set<Integer> pairCandidates = np1.getCandidates();
                    for(int j2 = j1 + 1; j2 < GRID_SIZE; j2++){
                        Cell np2 = grid.getCell(j2, i);
                        // ce druga celica ni resena in ima enake kandidate si dubu par
                        if(!np2.isSolved() && np2.getCandidates().equals(pairCandidates)){
                            // TYPE 1
                            Integer[] temp = new Integer[2];
                            np1.getCandidates().toArray(temp);
                            for(int l = 0; l < GRID_SIZE; l++){
                                // naredi isto kot opisano v predelku za vrstico, samo da gleda stolpec
                                if(l != i && grid.getCell(j1, l).getCandidates().equals(np1.getCandidates()) && grid.getCell(j2, l).getCandidates().size() >= 2 && (grid.getCell(j2, l).getCandidates().contains(temp[0]) || grid.getCell(j2, l).getCandidates().contains(temp[1]))){
                                    // System.out.println("C1 "+j1+i+" "+j2+i+" "+l+j2+" "+temp[0]+temp[1]);
                                    grid.getCell(j2, l).removeCandidate(temp[0]);
                                    grid.getCell(j2, l).removeCandidate(temp[1]);
                                }
                                if(l != i && grid.getCell(j2, l).getCandidates().equals(np1.getCandidates()) && grid.getCell(j1, l).getCandidates().size() >= 2 && (grid.getCell(j1, l).getCandidates().contains(temp[0]) || grid.getCell(j1, l).getCandidates().contains(temp[1]))){
                                    // System.out.println("C1 "+j1+i+" "+j2+i+" "+l+j1+" "+temp[0]+temp[1]);
                                    grid.getCell(j1, l).removeCandidate(temp[0]);
                                    grid.getCell(j1, l).removeCandidate(temp[1]);
                                }
                            }
                            // TYPE 2
                            Integer[] temp2 = new Integer[2];
                            np1.getCandidates().toArray(temp2);
                            for(int l = 0; l < GRID_SIZE; l++){
                                // gre skozi ce dobi dve enaki celici v istem stolpci, ki imata oba kandidata para in se skupnega tretjega 
                                if(l != i && grid.getCell(j1, l).getCandidates().size() == 3 && grid.getCell(j1, l).getCandidates().contains(temp2[0]) && grid.getCell(j1, l).getCandidates().contains(temp2[1]) && grid.getCell(j1, l).getCandidates().equals(grid.getCell(j2, l).getCandidates())){
                                    // System.out.println("C2 "+i+j1+" "+i+j2+" "+l);
                                    for(int m = 0; m < GRID_SIZE; m++){
                                        for(Integer tretjiK : grid.getCell(j1, l).getCandidates()){
                                            if(tretjiK == temp2[0] || tretjiK == temp2[1]) continue;
                                            // odstrani tretjega kandidata v tem stolpci, razen v prej dobljenih celicah
                                            if(m != j1 && m != j2) grid.getCell(l, m).removeCandidate(tretjiK);
                                            int boxR = 3 * (j1 / 3) + (m / 3);
                                            int boxC = 3 * (l / 3) + (m % 3);
                                            // ce sta dobljeni celici v istem boxu, remova kandidata se v boxu
                                            if(j1 / 3 == j2 / 3 && !(boxC == l && (boxR == j1 || boxR == j2))){
                                                grid.getCell(boxR, boxC).removeCandidate(tretjiK);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // funkcija odstrani kandidate, ki vidijo obe "barvi" chaina ali kandidata, ki je dvakrat povezan z sibko povezavo (liho st clenov) ali vnese kandidata, ki je dvakrat povezan z mocno povezavo (3 cleni z mocno povezavo, liho st clenov)
    public void elimXCycles(Grid grid){
        Counters.addToCounter("CALL",  1);
        // System.out.println("X Cycles");
        for(int k = 1; k < GRID_SIZE + 1; k++){
            // System.out.println("k "+k);
            // kljuc: stevilo, vsebina: seznam celic v chainu 
            Map<Integer, List<Cell>> cycleParts = new HashMap<>();
            int key = 0;
            for(int r = 0; r < GRID_SIZE; r++){
                for(int c = 0; c < GRID_SIZE; c++){
                    Cell cell = grid.getCell(r, c);
                    // kljuc = celica, vrednost = "barva" 0,1
                    Map<Cell, Integer> mapaBarv = new HashMap<>();
                    // gre naprej samo ce je celica prazna, ima kandidata in se NI del nekega chaina
                    if(!cell.isSolved() && cell.getCandidates().contains(k) && !mapaBarv.containsKey(grid.getCell(r, c))){
                        cChainOK(grid, cell, k, mapaBarv, 0, cycleParts, ++key);
                    }
                }
            }
            int[]cycKeys = new int[cycleParts.keySet().size()];
            Iterator<Integer> it = cycleParts.keySet().iterator();
            for(int i = 0; i < cycleParts.keySet().size(); i++){
                cycKeys[i] = it.next();
            }
            ArrayList<int[]> res = permute(cycKeys);

            /* for(int i = 0; i < res.size(); i++){
                for(int j = 0; j < res.get(i).length; j++){
                    System.out.print(res.get(i)[j]+" ");
                }
                System.out.println();
            } */

            if(!res.isEmpty()){
                for(int i = 0; i < res.size(); i++){
                    // System.out.println("p"+i);
                    Set<Integer> vrstica = new HashSet<Integer>();
                    Set<Integer> stolpec = new HashSet<Integer>();
                    Cell startCell = cycleParts.get(res.get(i)[0]).get(0);
                    // stevilka vrstice, stolpca, boxa za pogledat za naslednji del cycla
                    int crS = startCell.getRow();
                    int ccS = startCell.getColumn();
                    int cr = cycleParts.get(res.get(i)[0]).get(1).getRow();
                    int cc = cycleParts.get(res.get(i)[0]).get(1).getColumn();
                    vrstica.add(crS);
                    vrstica.add(cr);
                    stolpec.add(ccS);
                    stolpec.add(cc);
                    for(int j = 1; j < res.get(i).length; j++){
                        Cell firstLink = cycleParts.get(res.get(i)[j]).get(0);
                        Cell secndLink = cycleParts.get(res.get(i)[j]).get(1);
                        // stevilka vrstice, stolpca, boxa za pogledat za naslednji del cycla
                        int cr1 = firstLink.getRow();
                        int cc1 = firstLink.getColumn();
                        int cr2 = secndLink.getRow();
                        int cc2 = secndLink.getColumn();
                        if(j != 1 && (cr == crS || cc == ccS || (cr/3 == crS/3 && cc/3 == ccS/3))){
                            for(int r : vrstica){
                                for(int c : stolpec){
                                    Cell elimCell = grid.getCell(r,c);
                                    boolean pravaCelica = true;
                                    for(int l : cycleParts.keySet()){
                                        if(cycleParts.get(l) == null)
                                            continue;
                                        // skipne celica v chain cyclu
                                        if(cycleParts.get(l).contains(elimCell))
                                            pravaCelica = false;
                                    }
                                    if(pravaCelica){
                                        // System.out.println(r+""+c+" "+k);
                                        elimCell.removeCandidate(k);
                                    }
                                }
                            }
                            break;
                        }
                        // "poveze" dva chaina
                        if(cr == cr1 || cc == cc1 || (cr/3 == cr1/3 && cc/3 == cc1/3)){
                            cr = cr2;
                            cc = cc2;
                            vrstica.add(cr1);
                            vrstica.add(cr2);
                            stolpec.add(cc1);
                            stolpec.add(cc2);
                            continue;
                        }
                        if(cr == cr2 || cc == cc2 || (cr/3 == cr2/3 && cc/3 == cc2/3)){
                            cr = cr1;
                            cc = cc1;
                            vrstica.add(cr1);
                            vrstica.add(cr2);
                            stolpec.add(cc1);
                            stolpec.add(cc2);
                            continue;
                        }
                        break;
                    }
                }
            }
        }
    }
    // funkcija naredi in pogleda ali je chain pravilen, ce je pravilen vrne true, cene false
    public int cChainOK(Grid grid, Cell startCell, int k, Map<Cell, Integer> mapaBarv, int barva, Map<Integer, List<Cell>> cycleParts, int cycPKey){
        Queue<Cell> vrsta = new LinkedList<>();
        vrsta.add(startCell);
        mapaBarv.put(startCell, 0);
        int nextBarva;
        // kljuci: 0 = row, 1 = col, 2 = box
        Map<Integer, List<Integer>> mapaPozicij = new HashMap<>();
        int stClenov = 0;
        int st3links = 0;
        boolean linkChain3 = false;
        // loop dokler so mozne naslednje celice, vhodne celice
        while(!vrsta.isEmpty()){
            stClenov++;
            mapaPozicij.put(0, new ArrayList<>());
            mapaPozicij.put(1, new ArrayList<>());
            mapaPozicij.put(2, new ArrayList<>());
            Cell cell = vrsta.poll();
            nextBarva = mapaBarv.get(cell) == 0 ? 1 : 0;
            Set<Cell> links = new HashSet<>();
            int r = cell.getRow();
            int c = cell.getColumn();
            // loop za napounit mapo, da vidis kolk je kandidatov v row/col/box
            for(int i = 0; i < GRID_SIZE; i++){
                // ROW
                if(i != c && !grid.getCell(r, i).isSolved() && grid.getCell(r, i).getCandidates().contains(k)){
                    mapaPozicij.get(0).add(i);
                } 
                // COL
                if(i != r && !grid.getCell(i, c).isSolved() && grid.getCell(i, c).getCandidates().contains(k)){
                    mapaPozicij.get(1).add(i);
                }
                // BOX
                int boxR = 3 * (r / 3) + (i / 3);
                int boxC = 3 * (c / 3) + (i % 3);
                if((boxR != r || boxC != c) && !grid.getCell(boxR, boxC).isSolved() && grid.getCell(boxR, boxC).getCandidates().contains(k)){
                    mapaPozicij.get(2).add(i);
                }
            }
            // ce je kandidat samo edn (skipnli smo celico, za katero gledamo "sosede"), dodaj celico v vrsto 
            if(mapaPozicij.get(0) != null ? mapaPozicij.get(0).size() == 1 && !links.contains(grid.getCell(r, mapaPozicij.get(0).get(0))) : false){
                links.add(grid.getCell(r, mapaPozicij.get(0).get(0)));
            }
            if(mapaPozicij.get(1) != null ? mapaPozicij.get(1).size() == 1 && !links.contains(grid.getCell(mapaPozicij.get(1).get(0), c)) : false){
                links.add(grid.getCell(mapaPozicij.get(1).get(0), c));
            }
            if(mapaPozicij.get(2) != null ? mapaPozicij.get(2).size() == 1 && !links.contains(grid.getCell(3 * (r / 3) + (mapaPozicij.get(2).get(0) / 3), 3 * (c / 3) + (mapaPozicij.get(2).get(0) % 3))) : false){
                links.add(grid.getCell(3 * (r / 3) + (mapaPozicij.get(2).get(0) / 3), 3 * (c / 3) + (mapaPozicij.get(2).get(0) % 3)));
            }
            // loop po mogocih naslednjih celicah
            for(Cell nextCell : links){
                // se pogleda ce ima naslednja celica ze doloceno barvo
                if(mapaBarv.containsKey(nextCell)){
                    // ce ima naslednja celica enako barvo kot trenutna celica, je napacn chain
                    if(mapaBarv.get(nextCell).equals(mapaBarv.get(cell))) return -1;
                }else{
                    // ce naslednja celica se nima dolocene barve, se to naredi
                    mapaBarv.put(nextCell, nextBarva);
                    // se oznace da je celica del nekega chaina in se jo doda v vrsto, da se pogleda mogoce njene naslednje linke chaina
                    vrsta.add(nextCell);
                }
            }
            // ce ima katerakoli celica 3 povezave, to ni prava postavitev
            if(links.size() == 3) return -1;
            if(links.size() == 2 && !linkChain3){
                st3links++;
                linkChain3 = true;
            }else if(links.size() == 2 && linkChain3) st3links++;
            // celica, ki nima nobenih povezav se odstrani iz mape
            if(vrsta.isEmpty() && cell == startCell){
                mapaBarv.remove(cell);
                return -1;
            }
        }
        cycleParts.put(cycPKey, new ArrayList<>());
        for(Map.Entry<Cell, Integer> entry : mapaBarv.entrySet()){
            // prepreci podvajanje podatkov pod razlicnimi kljuci
            for (Map.Entry<Integer, List<Cell>> cycp : cycleParts.entrySet()) {
                if(cycp.getValue().contains(entry.getKey())){
                    cycleParts.remove(cycPKey);
                    break;
                }
            }
            if(!cycleParts.keySet().contains(cycPKey)) break;
            cycleParts.get(cycPKey).add(entry.getKey());
        }
        return 1;
    }
    // funkcija za swapat 2 elementa arraya
    public void swap(int[] keys, int a, int b){
        int t = keys[a];
        keys[a] = keys[b];
        keys[b] = t;
    }
    // funkcija za poiskat vse permutacije
    public void findPermutations(ArrayList<int[]> perm, int[] keys, int a, int b){
        if(a == b){
            perm.add(Arrays.copyOf(keys, keys.length));
            return;
        }
        for(int i = a; i <= b; i++){
            swap(keys, a, i);
            findPermutations(perm, keys, a+1, b);
            swap(keys, a, i);
        }
    }
    // funkcija za returnat permutacije
    public ArrayList<int[]> permute(int[] keys){
        ArrayList<int[]> perm = new ArrayList<int[]>();
        int x = keys.length - 1;
        findPermutations(perm, keys, 0, x);
        return perm;
    }

    // fukcija ki dela podobno kot Swordfish
	public void elimFinSwordfish(Grid grid){
        Counters.addToCounter("CALL",  1);
		// System.out.println("Fin Swordfish");
		// loop po kandidatih
		for(int k = 1; k < GRID_SIZE + 1; k++){
			// loop za prve 2-3 celice
            for(int c1 = 0; c1 < GRID_SIZE - 2; c1++){
                // loop za druge 2-3 celice
                for(int c2 = c1 + 1; c2 < GRID_SIZE - 1; c2++){
                    // loop za zadnje 2-3 celice
                    for(int c3 = c2 + 1; c3 < GRID_SIZE; c3++){
                        Map<Integer, List<Integer>> mapaPozicijR = new HashMap<>();
                        Map<Integer, List<Integer>> mapaPozicijC = new HashMap<>();
						// loop za napolnit mapo
                        for(int i = 0; i < GRID_SIZE; i++){
							// ce ima celica v prvi VRSTICI kandidata
                            if(grid.getCell(c1, i).getCandidates().contains(k)){
                                // kljuc == st 0
                                mapaPozicijR.putIfAbsent(0, new ArrayList<>());
                                // vrednost == pozicija v vrstici
                                mapaPozicijR.get(0).add(i);
                            }
                            if(grid.getCell(c2, i).getCandidates().contains(k)){
                                mapaPozicijR.putIfAbsent(1, new ArrayList<>());
                                mapaPozicijR.get(1).add(i);
                            }
                            if(grid.getCell(c3, i).getCandidates().contains(k)){
                                mapaPozicijR.putIfAbsent(2, new ArrayList<>());
                                mapaPozicijR.get(2).add(i);
                            }
                            // ce ima celica v prvem STOLPCI kandidata
                            if(grid.getCell(i, c1).getCandidates().contains(k)){
                                // kljuc == st 0
                                mapaPozicijC.putIfAbsent(0, new ArrayList<>());
                                // vrednost == pozicija v vrstici
                                mapaPozicijC.get(0).add(i);
                            }
                            if(grid.getCell(i, c2).getCandidates().contains(k)){
                                mapaPozicijC.putIfAbsent(1, new ArrayList<>());
                                mapaPozicijC.get(1).add(i);
                            }
                            if(grid.getCell(i, c3).getCandidates().contains(k)){
                                mapaPozicijC.putIfAbsent(2, new ArrayList<>());
                                mapaPozicijC.get(2).add(i);
                            }
						}
						//gleda COL, remova ROW
						// pogleda da so vse 3 vrstice polne, pogleda da ali imajo vse vrstice 2 al 3 ali pa ce imajo 2 vrstici 2 al 3 in ena 4 al 5
						if((mapaPozicijC.get(0) != null && mapaPozicijC.get(1) != null && mapaPozicijC.get(2) != null) && (((mapaPozicijC.get(0).size() == 2 || mapaPozicijC.get(0).size() == 3) && (mapaPozicijC.get(1).size() == 2 || mapaPozicijC.get(1).size() == 3) && (mapaPozicijC.get(2).size() == 2 || mapaPozicijC.get(2).size() == 3)) || (((mapaPozicijC.get(0).size() == 4 || mapaPozicijC.get(0).size() == 5) && (mapaPozicijC.get(1).size() == 2 || mapaPozicijC.get(1).size() == 3) && (mapaPozicijC.get(2).size() == 2 || mapaPozicijC.get(2).size() == 3)) || ((mapaPozicijC.get(0).size() == 2 || mapaPozicijC.get(0).size() == 3) && (mapaPozicijC.get(1).size() == 4 || mapaPozicijC.get(1).size() == 5) && (mapaPozicijC.get(2).size() == 2 || mapaPozicijC.get(2).size() == 3)) || ((mapaPozicijC.get(0).size() == 2 || mapaPozicijC.get(0).size() == 3) && (mapaPozicijC.get(1).size() == 2 || mapaPozicijC.get(1).size() == 3) && (mapaPozicijC.get(2).size() == 4 || mapaPozicijC.get(2).size() == 5))))){
							//dodaj da bo gledalo kolkokrat se ponovi vsako stevilo iz unijePozicij
							//kljuc = stR, vrednost = st ponovitev
							Map<Integer, Integer> mapaR = new HashMap<>();
                            Set<Integer> unijaPozicij = new HashSet<>(mapaPozicijC.get(0));
                            unijaPozicij.addAll(mapaPozicijC.get(1));
                            unijaPozicij.addAll(mapaPozicijC.get(2));
                            if(unijaPozicij.size() > 3 && unijaPozicij.size() < 6){
								//loop za napolnit mapaR
								for(int mr = 0; mr < 3; mr++){
									for(int mrSt : mapaPozicijC.get(mr)){
										//ce ne doda v mapo zapisa se koda ifa izvede
										if(mapaR.putIfAbsent(mrSt,1) != null){
											//poveca vrednost +1 kjer je kljuc mrSt
											mapaR.put(mrSt, mapaR.get(mrSt)+1);
										}
									}
								}
								//za dobit min vrednost
								int min = GRID_SIZE;
								for(int mapaKey : mapaR.keySet()){
									if(mapaR.get(mapaKey) < min){
										min = mapaR.get(mapaKey);
									}
								}
								//loop za dobit vse stevilke vrstic kjer se pojavi ta min vrednost (pozicije finov)
								Set<Integer> keys = new HashSet<Integer>();
								//tle so shranjene pozicije ki so del Swordfisha
								Set<Integer> fishParts = new HashSet<Integer>();
								for(int mapaKey : mapaR.keySet()){
									if(mapaR.get(mapaKey) == min){
										keys.add(mapaKey);
									}else{
										fishParts.add(mapaKey);
									}
								}
								if(keys.size() > 2 || fishParts.size() > 3) continue;
								//izracunej in shrani st boxa
								Integer[] keysArr = new Integer[keys.size()];
								keysArr = keys.toArray(keysArr);
								int bn1 = keysArr[0] /3;
								int mapaNo = -1;
								if(mapaPozicijC.get(0).contains(keysArr[0])){
									mapaNo = c1;
								}else if(mapaPozicijC.get(1).contains(keysArr[0])){
									mapaNo = c2;
								}else if(mapaPozicijC.get(2).contains(keysArr[0])){
									mapaNo = c3;
								}
								if(keys.size() == 2){
									int bn2 = keysArr[1] /3;
									//ce oba fina nista v istem boxu pejd iskat naprej
									if(bn1 != bn2) continue;
									int mapaNo2 = -1;
									if(mapaPozicijC.get(0).contains(keysArr[1])){
										mapaNo2 = c1;
									}else if(mapaPozicijC.get(1).contains(keysArr[1])){
										mapaNo2 = c2;
									}else if(mapaPozicijC.get(2).contains(keysArr[1])){
										mapaNo2 = c3;
									}
									if(mapaNo != mapaNo2) continue;
								}
								if(mapaNo < 0) continue;
								
								for(int i = 0; i < 3; i++){
									//za dobit vrstico kjer se remova
									if(fishParts.contains(bn1*3+i)){
										for(int j = 0; j < 3; j++){
											if((mapaNo/3)*3+j != c1 && (mapaNo/3)*3+j != c2 && (mapaNo/3)*3+j != c3){
												grid.getCell(bn1*3+i, (mapaNo/3)*3+j).removeCandidate(k);
												// System.out.println("RMR "+(bn1*3+i)+" "+((mapaNo/3)*3+j)+" k"+k);
											}
										}
									}
								}
                            }
                        }
						//gleda ROW remova COL
						// pogleda da so vse 3 vrstice polne, pogleda da ali imajo vse vrstice 2 al 3 ali pa ce imajo 2 vrstici 2 al 3 in ena 4 al 5
						if((mapaPozicijR.get(0) != null && mapaPozicijR.get(1) != null && mapaPozicijR.get(2) != null) && (((mapaPozicijR.get(0).size() == 2 || mapaPozicijR.get(0).size() == 3) && (mapaPozicijR.get(1).size() == 2 || mapaPozicijR.get(1).size() == 3) && (mapaPozicijR.get(2).size() == 2 || mapaPozicijR.get(2).size() == 3)) || (((mapaPozicijR.get(0).size() == 4 || mapaPozicijR.get(0).size() == 5) && (mapaPozicijR.get(1).size() == 2 || mapaPozicijR.get(1).size() == 3) && (mapaPozicijR.get(2).size() == 2 || mapaPozicijR.get(2).size() == 3)) || ((mapaPozicijR.get(0).size() == 2 || mapaPozicijR.get(0).size() == 3) && (mapaPozicijR.get(1).size() == 4 || mapaPozicijR.get(1).size() == 5) && (mapaPozicijR.get(2).size() == 2 || mapaPozicijR.get(2).size() == 3)) || ((mapaPozicijR.get(0).size() == 2 || mapaPozicijR.get(0).size() == 3) && (mapaPozicijR.get(1).size() == 2 || mapaPozicijR.get(1).size() == 3) && (mapaPozicijR.get(2).size() == 4 || mapaPozicijR.get(2).size() == 5))))){
							//dodaj da bo gledalo kolkokrat se ponovi vsako stevilo iz unijePozicij
							//kljuc = stR, vrednost = st ponovitev
							Map<Integer, Integer> mapaC = new HashMap<>();
                            Set<Integer> unijaPozicij = new HashSet<>(mapaPozicijR.get(0));
                            unijaPozicij.addAll(mapaPozicijR.get(1));
                            unijaPozicij.addAll(mapaPozicijR.get(2));
                            if(unijaPozicij.size() > 3 && unijaPozicij.size() < 6){
								//loop za napolnit mapaC
								for(int mc = 0; mc < 3; mc++){
									for(int mcSt : mapaPozicijR.get(mc)){
										//ce ne doda v mapo zapisa se koda ifa izvede
										if(mapaC.putIfAbsent(mcSt,1) != null){
											//poveca vrednost +1 kjer je kljuc mcSt
											mapaC.put(mcSt, mapaC.get(mcSt)+1);
										}
									}
								}
								//za dobit min vrednost
								int min = GRID_SIZE;
								for(int mapaKey : mapaC.keySet()){
									if(mapaC.get(mapaKey) < min){
										min = mapaC.get(mapaKey);
									}
								}
								//loop za dobit vse stevilke vrstic kjer se pojavi ta min vrednost (pozicije finov)
								Set<Integer> keys = new HashSet<Integer>();
								//tle so shranjene pozicije ki so del Swordfisha
								Set<Integer> fishParts = new HashSet<Integer>();
								for(int mapaKey : mapaC.keySet()){
									if(mapaC.get(mapaKey) == min){
										keys.add(mapaKey);
									}else{
										fishParts.add(mapaKey);
									}
								}
								if(keys.size() > 2 || fishParts.size() > 3) continue;
								//izracunej in shrani st boxa
								Integer[] keysArr = new Integer[keys.size()];
								keysArr = keys.toArray(keysArr);
								int bn1 = keysArr[0] /3;
								int mapaNo = -1;
								if(mapaPozicijR.get(0).contains(keysArr[0])){
									mapaNo = c1;
								}else if(mapaPozicijR.get(1).contains(keysArr[0])){
									mapaNo = c2;
								}else if(mapaPozicijR.get(2).contains(keysArr[0])){
									mapaNo = c3;
								}
								if(keys.size() == 2){
									int bn2 = keysArr[1] /3;
									//ce oba fina nista v istem boxu pejd iskat naprej
									if(bn1 != bn2) continue;
									int mapaNo2 = -1;
									if(mapaPozicijR.get(0).contains(keysArr[1])){
										mapaNo2 = c1;
									}else if(mapaPozicijR.get(1).contains(keysArr[1])){
										mapaNo2 = c2;
									}else if(mapaPozicijR.get(2).contains(keysArr[1])){
										mapaNo2 = c3;
									}
									if(mapaNo != mapaNo2) continue;
								}
								if(mapaNo < 0) continue;
								
								for(int i = 0; i < 3; i++){
									//za dobit vrstico kjer se remova
									if(fishParts.contains(bn1*3+i)){
										for(int j = 0; j < 3; j++){
											if((mapaNo/3)*3+i != c1 && (mapaNo/3)*3+i != c2 && (mapaNo/3)*3+i != c3){
												grid.getCell((mapaNo/3)*3+i, bn1*3+j).removeCandidate(k);
												// System.out.println("RMC "+((mapaNo/3)*3+i)+" "+(bn1*3+j)+" k"+k);
											}
										}
									}
								}
                            }
                        }
					}
				}
			}
		}
	}

    //funkcija odstrani kandidata, ki ga odstranimo lahko na 2 nacina, ko sledimo chainu, klice singles funkcijo
	public void elimForcingChain(Grid grid){
        Counters.addToCounter("CALL",  1);
		// System.out.println("Forcing Chain");
		//loop po vseh celicah, ki majo 2 kandidata
		for(int r = 0; r < GRID_SIZE; r++){
			for(int c = 0; c < GRID_SIZE; c++){
				if(grid.getCell(r,c).getCandidates().size() != 2) continue;
				//nekaj za shranit celice z odstranjenimi kandidati
				/* //kljuc = string "r c", vrednost = kandidat, ki je "odstranjen"
				Map<String, List<Integer>> mapaOdstranjenih1 = new HashMap<>();
				Map<String, List<Integer>> mapaOdstranjenih2 = new HashMap<>(); */
				Grid gridk1 = new Grid();
				gridk1.copyGrid(grid);
				Grid gridk2 = new Grid();
				gridk2.copyGrid(grid);
				Integer[] sckArr = new Integer[2];
				int cc = GRID_SIZE*GRID_SIZE*GRID_SIZE;
				//ko dobimo celico z 2 kandidatoma loop da izvedemo kodo za vsakega kandidata
				for(int sck : grid.getCell(r,c).getCandidates()){
					sckArr = grid.getCell(r,c).getCandidates().toArray(sckArr);
					Grid g = sckArr[0] == sck ? gridk1 : gridk2;
					g.getCell(r,c).setValue(sck);
					//loop da izpolne kolikor upa
					do{
						cc = g.getCandidateCount();
						fillSingles(g);
					}while(cc != g.getCandidateCount());
				}
				//loop za primerjavo gridov
				for(int i = 0; i < GRID_SIZE; i++){
					for(int j = 0; j < GRID_SIZE; j++){
						//ce je celica clue (zapolnjena za v podanem puzlu) jo preskoci
						if(grid.getCell(i,j).isClue()) continue;
						Cell gk1c = gridk1.getCell(i,j);
						Cell gk2c = gridk2.getCell(i,j);
						Integer[] gridArr = new Integer[grid.getCell(i,j).getCandidates().size()];
						gridArr = grid.getCell(i,j).getCandidates().toArray(gridArr);
						//loop po kandidatih iz glavnega grida
						for(int k = 0; k < gridArr.length; k++){
							//kandidata se odstrani samo ko se ga odstrani v obeh temporarnih gridih
							if(((gk1c.isSolved() && gk1c.getValue() == gridArr[k]) || (gk2c.isSolved() && gk2c.getValue() == gridArr[k])) || ((gk1c.isSolved() && gk1c.getValue() == gridArr[k]) || (!gk2c.isSolved() && gk2c.getCandidates().contains(gridArr[k]))) || ((gk1c.isSolved() && gk1c.getValue() == gridArr[k]) || (!gk2c.isSolved() && !gk2c.getCandidates().contains(gridArr[k]))) || ((!gk1c.isSolved() && gk1c.getCandidates().contains(gridArr[k])) || (gk2c.isSolved() && gk2c.getValue() == gridArr[k])) || ((!gk1c.isSolved() && !gk1c.getCandidates().contains(gridArr[k])) || (gk2c.isSolved() && gk2c.getValue() == gridArr[k])) || ((!gk1c.isSolved() && gk1c.getCandidates().contains(gridArr[k])) || (!gk2c.isSolved() && gk2c.getCandidates().contains(gridArr[k]))) || ((!gk1c.isSolved() && !gk1c.getCandidates().contains(gridArr[k])) || (!gk2c.isSolved() && gk2c.getCandidates().contains(gridArr[k]))) || ((!gk1c.isSolved() && gk1c.getCandidates().contains(gridArr[k])) ||(!gk2c.isSolved() && !gk2c.getCandidates().contains(gridArr[k])))){
								continue;
							}else if(!gk1c.getCandidates().contains(gridArr[k]) || !gk2c.getCandidates().contains(gridArr[k])){
								// System.out.println(""+r+c+" "+i+j+" k"+gridArr[k]);
								grid.getCell(i,j).removeCandidate(gridArr[k]);
							}
						}
					}
				}
			}
		}
	}
	

  @Override
  protected Output execute(Input input) {
    Grid grid = new Grid();
    initializeGrid(grid, input.sudoku);
    int cc = grid.getCandidateCount();
    int ccAfterFunc = cc;
    while(cc != 0){
      // klices se funkcije po enakem postopku
      elimForcingChain(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimFinSwordfish(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimXCycles(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimUniqRect(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimJellyfish(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }

      elimAvoidableRect(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimBUG(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimXYZWing(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimSwordfish(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimRectangle(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimYWing(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimSinglesChain(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimXWing(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimBoxLineReduct(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimPointing(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      
      elimHiddenQuads(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimNakedQuads(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimHiddenTriples(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimHiddenPairs(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimNakedTriples(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      elimNakedPairs(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      fillHiddenSingles(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      fillSingles(grid);
      ccAfterFunc = grid.getCandidateCount();
      if(ccAfterFunc < cc){
        cc = ccAfterFunc;
        continue;
      }
      if(cc == ccAfterFunc) break;
    }
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

