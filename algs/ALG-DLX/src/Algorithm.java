import java.util.*;
import java.io.*;

/**
 * Algorithm for project SudokuSolver.
 *
 * @author ...
 */
public class Algorithm extends ProjectAbstractAlgorithm {
  public int SIZE = 3, N = 9;
	public int[][] GRID = new int[N][N];
	
	public ColumnNode root = null;
	public ArrayList<Node> solution = new ArrayList<>();

	class Node implements Serializable{
		transient Node left, right, up, down;
		ColumnNode head;
		
		Node(){ }
	}
	
	//head node stores column info
	class ColumnNode extends Node{
		int size = 0;
		ColumnID info;
		
		ColumnNode(){ }
	}
	
	//helps store info of column
	class ColumnID implements Serializable{
		int constraint = -1;
		int number = -1;
		int position = -1;
		
		ColumnID(){ }
	}
	
	public void run(int[][] puzzleTab){
		byte[][] matrix = createMatrix(puzzleTab);
		ColumnNode doubleLinkedList = createDoubleLinkedLists(matrix);
		search(0);
	}
	
	public byte[][] createMatrix(int[][] puzzleTab){
		ArrayList<int[]> cluesList = new ArrayList<>();
		//counter podanih stevil
		int counter = 0;
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				if(puzzleTab[r][c] > 0){
					//shrani st, row in col
					cluesList.add(new int[]{puzzleTab[r][c],r,c});
					counter++;
				}
			}
		}
		//shrani podana stevila
		int[][] clues = new int[counter][];
		for(int i = 0; i < counter; i++){
			clues[i] = (int[])cluesList.get(i);
		}
		//0-1 matrica velikosti 729x324
		//N rows * N cols * N num, N rows * N cols * 4 constraints
		byte[][] matrix = new byte[N*N*N][4*N*N];
		//loop po 1. dimenziji matrice 729
		for(int d = 0; d < N; d++){
			for(int r = 0; r < N; r++){
				for(int c = 0; c < N; c++){
					//pogleda ce lahko vstavimo d v polje (r,c)
					if(!filled(d,r,c,clues)){
						int rowIndex = c + r*N + N*N*d;
						//the 4 constraints set to 1
						int blkIndex = (c/SIZE)+(r/SIZE)*SIZE;
						int colIndexR = 3*N*d+r;
						int colIndexC = 3*N*d+N+c;
						int colIndexB = 3*N*d+2*N+blkIndex;
						int colIndexSimpl = 3*N*N+(c+N*r);
						matrix[rowIndex][colIndexR] = 1;
						matrix[rowIndex][colIndexC] = 1;
						matrix[rowIndex][colIndexB] = 1;
						matrix[rowIndex][colIndexSimpl] = 1;
					}
				}
			}
		}
		return matrix;
	}
	
	//pogleda ce lahko vstavimo digit v polje
	public boolean filled(int digit, int row, int col, int[][] clues){
		boolean filled = false;
		if(clues != null){
			for(int i = 0; i < clues.length; i++){
				//-1 zato ker je d(digit) loop 0-8 
				int d = clues[i][0]-1;
				int r = clues[i][1];
				int c = clues[i][2];
				
				int blkStartIndCol = (c/SIZE)*SIZE;
				int blkEndIndCol = blkStartIndCol + SIZE;
				int blkStartIndRow = (r/SIZE)*SIZE;
				int blkEndIndRow = blkStartIndRow + SIZE;
				//ce je ista pozicija ku u tabeli
				if(d != digit && r == row && c == col){
					filled = true;
				//ce je stevilka ze v stolpcu al vrstici
				}else if(d == digit && (row == r || col == c) && !(row == r && col == c)){
					filled = true;
				//ce je stvilka ze v boxu
				}else if(d == digit && row > blkStartIndRow && row < blkEndIndRow && col > blkStartIndCol && col < blkEndIndCol && !(row == r && col == c)){
					filled = true;
				}
			}
		}
		return filled;
	}
	
	//matrico pretvori v dvojno povezan ciklicni seznam za uporablo metode dancing links
	public ColumnNode createDoubleLinkedLists(byte[][] matrix){
		root = new ColumnNode();
		ColumnNode curCol = root;
		for(int col = 0; col < matrix[0].length; col++){
			ColumnID id = new ColumnID();
			if(col < 3*N*N){
				int digit = col/(3*N)+1;
				id.number = digit;
				int index = col-(digit-1)*3*N;
				//row
				if(index < N){
					id.constraint = 0;
					id.position = index;
				//col
				}else if(index < 2*N){
					id.constraint = 1;
					id.position = index-N;
				//box
				}else{
					id.constraint = 2;
					id.position = index-2*N;
				}
			}else{
				id.constraint = 3;
				id.position = col-3*N*N;
			}
			//naredimo nov ColNode, ga povezemo s head Nodeom in se premaknemo na ta nov Node
			curCol.right = new ColumnNode();
			curCol.right.left = curCol;
			curCol = (ColumnNode)curCol.right;
			//dodamo info stolpcu
			curCol.info = id;
			//head kaze nase ker je on head stolpca
			curCol.head = curCol;
		}
		//povezi zadnji colNode z rootom da rata ciklicno povezano
		curCol.right = root;
		root.left = curCol;
		
		for(int row = 0; row < matrix.length; row++){
			curCol = (ColumnNode)root.right;
			Node lastCreatedElement = null;
			Node firstElement = null;
			for(int col = 0; col < matrix[row].length; col++){
				if(matrix[row][col] == 1){
					Node colElement = curCol;
					while(colElement.down != null){
						colElement = colElement.down;
					}
					colElement.down = new Node();
					if(firstElement == null){
						firstElement = colElement.down;
					}
					colElement.down.up = colElement;
					colElement.down.left = lastCreatedElement;
					colElement.down.head = curCol;
					if(lastCreatedElement != null){
						colElement.down.left.right = colElement.down;
					}
					lastCreatedElement = colElement.down;
					curCol.size++;
				}
				curCol = (ColumnNode)curCol.right;
			}
			//naredis se stolpce ciklicne
			if(lastCreatedElement != null){
				lastCreatedElement.right = firstElement;
				firstElement.left = lastCreatedElement;
			}
		}
		curCol = (ColumnNode)root.right;
		for(int i = 0; i < matrix[0].length; i++){
			Node colElement = curCol;
			while(colElement.down != null){
				colElement = colElement.down;
			}
			colElement.down = curCol;
			curCol.up = colElement;
			curCol = (ColumnNode)curCol.right;
		}
		return root;
	}
	
	public void search(int k){
		//ko nam zmanjka stolpcev smo resili exact cover problem
		if(root.right == root){
			saveToGrid();
			return;
		}
		//izberemo column
		ColumnNode c = choose();
		cover(c);
		Node r = c.down;
		while(r != c){
			if(k < solution.size()){
				solution.remove(k);
			}
			solution.add(k,r);
			
			Node j = r.right;
			while(j != r){
				cover(j.head);
				j = j.right;
			}
			//rekurzivno iskanje
			search(k+1);
			
			Node r2 = (Node)solution.get(k);
			Node j2 = r2.left;
			while(j2 != r2){
				uncover(j2.head);
				j2 = j2.left;
			}
			r = r.down;
		}
		uncover(c);
	}
	
	public void saveToGrid(){
		int[] result = new int[N*N];
		//for(Iterator it = solution.iterator(); it.hasNext();){
		for(Node it : solution){
			int number = -1;
			int cellNo = -1;
			//Node element = (Node)it.next();
			Node element = (Node)it;
			Node next = element;
			do{
				if(next.head.info.constraint == 0){
					number = next.head.info.number;
				}else if(next.head.info.constraint == 3){
					cellNo = next.head.info.position;
				}
				next = next.right;
			}while(element != next);
			result[cellNo] = number;
		}
		//prepisemo result v grid
		int resultCounter = 0;
		for(int r = 0; r < N; r++){
			for(int c = 0; c < N; c++){
				GRID[r][c] = result[resultCounter];
				resultCounter++;
			}
		}
	}
	
	//poiscemo column kjer se najmanjkrat pojavi 1
	public ColumnNode choose(){
		ColumnNode rightOfRoot = (ColumnNode)root.right;
		ColumnNode smallest = rightOfRoot;
		while(rightOfRoot.right != root){
			rightOfRoot = (ColumnNode)rightOfRoot.right;
			if(rightOfRoot.size < smallest.size){
				smallest = rightOfRoot;
			}
		}
		return smallest;
	}
	
	public void cover(Node column){
    //@COUNT{CALL, 1}
		column.right.left = column.left;
		column.left.right = column.right;
		
		Node curRow = column.down;
		while(curRow != column){
			Node curNode = curRow.right;
			while(curNode != curRow){
				curNode.down.up = curNode.up;
				curNode.up.down = curNode.down;
				curNode.head.size--;
				curNode = curNode.right;
			}
			curRow = curRow.down;
		}
	}
	
	public void uncover(Node column){
    //@COUNT{CALL, 1}
		Node curRow = column.up;
		while(curRow != column){
			Node curNode = curRow.left;
			while(curNode != curRow){
				curNode.head.size++;
				curNode.down.up = curNode;
				curNode.up.down = curNode;
				curNode = curNode.left;
			}
			curRow = curRow.up;
		}
		column.right.left = column;
		column.left.right = column;
	}
	

  @Override
  protected Output execute(Input input) {
    //Fill
    for(int i = 0; i < N*N; i++){
      int row = i / N;
      int col = i % N;
      // prazne celice 0 ali . (. => -1) 
      int value = Character.getNumericValue(input.sudoku.charAt(i));
      // != dela ce so prazne 0, > dela ce so prazne 0 al .
      if(value > 0){
          GRID[row][col] = value;
      }
    }

    //run solver
    run(GRID);

    //Output
    String Algout = "";
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
          Algout += String.valueOf(GRID[i][j]);
      }
    }

    Output output = new Output(Algout);    
    return output;
  }

}