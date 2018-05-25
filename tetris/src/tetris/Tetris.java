package tetris;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Tetris extends Application {
	int[][] board= new int[40][20];
	int RowLimit=19;
	int ColumnLimit=9;
	int LowerBound=0;
	int[][]smallBoard=new int[4][4];
	int ROW,COLUMN=ColumnLimit/2,brick,nextBrick;
	int X=250;
	int Y=2*X;
	double size=X/10;	
	int smallBoardSize=25*4;
	int level=1,rowcount;
	double score;
	int line=0;
	double m;
	double fallingSpeed;
	int count;

	public static void main(String args[])
	{
		launch(args);
	}
	
	public void start(Stage primarystage)
	{
		
		primarystage.setTitle("the tetris game");	
		HBox root= new HBox();
		StackPane p= new StackPane();
		
		Canvas playingBoard = new Canvas(X,Y);
		playingBoard.setCursor(Cursor.HAND);
		p.getChildren().add(playingBoard);
		p.autosize();
		VBox information= new VBox(25);
		root.setPadding(new Insets(50,50,50,50));
        information.setPadding(new Insets(25,25,25,25));
        
        GridPane g = new GridPane();            
        
        Label sizeLabel= new Label("Size  ");
        Label rowLabel= new Label("Rows  ");
        Label columnLabel= new Label("Columns ");
        Label MLabel= new Label("M ");
        Label speedFactorLabel= new Label("Speed ");
        Label levelLabel = new Label("Level ");
        Label lineLabel = new Label("Line ");
        Label scoreLabel = new Label("Score ");

       
		Slider sizeadjuster = new Slider(250,350,250);
		sizeadjuster.setBlockIncrement(10);
		
		Slider rowadjuster = new Slider(19,39,1);
		rowadjuster.setBlockIncrement(1);
		rowadjuster.setShowTickLabels(true);

		
		Slider coladjuster = new Slider(9,15,1);
		coladjuster.setBlockIncrement(1);
		coladjuster.setShowTickLabels(true);

		
		Slider M = new Slider(1,10,1);
		M.setBlockIncrement(1);
		M.setShowTickLabels(true);

		Slider speedFactor = new Slider(0.1,1.0,0.1);
		speedFactor.setBlockIncrement(0.1);
		speedFactor.setShowTickLabels(true);
			
        g.add(sizeLabel, 0, 0);
        g.add(sizeadjuster, 1, 0);
        g.add(rowLabel,0,1);
        g.add(rowadjuster, 1, 1);
        g.add(columnLabel, 0, 2);
        g.add(coladjuster, 1, 2);
        g.add(MLabel, 0, 3);
        g.add(M, 1, 3);
        g.add(speedFactorLabel, 0, 4);
        g.add(speedFactor, 1, 4);
        g.add(lineLabel, 0, 5);
        g.add(levelLabel, 0, 6);
        g.add(scoreLabel, 0, 7);
        
		Canvas nextBoard = new Canvas(smallBoardSize,smallBoardSize);
		
		information.getChildren().addAll(nextBoard,g);

		root.setAlignment(Pos.CENTER);
		root.getChildren().addAll(p,information);

		GraphicsContext gc = playingBoard.getGraphicsContext2D();
		
		GraphicsContext gc2 = nextBoard.getGraphicsContext2D();
		
		gc.setStroke(Color.BLACK);
		
		gc.setFill(Color.WHITE);
		
		gc2.setStroke(Color.BLACK);
		
		gc2.setFill(Color.WHITE);
		
		
		gc2.fillRect(0,0, smallBoardSize, smallBoardSize);
		gc2.strokeRect(0,0, smallBoardSize, smallBoardSize);
		
		Text pause=new Text("PAUSE");
		pause.setFont(Font.font("Verdana",30));
		pause.setStroke(Color.AQUA);
		pause.setFill(Color.AQUA);
		
		Scene scene= new Scene(root,700,550);
		
		primarystage.setScene(scene);
		
		primarystage.show();
		brick=nextBrick();
		nextBrick=nextBrick();
		addNext();
		
		AnimationTimer loop = new AnimationTimer() {
			long l=0;
			public void handle(long now)
			{
				setRowLimit((int)rowadjuster.getValue());
				setColumnLimit((int)coladjuster.getValue());
				setX((int)sizeadjuster.getValue());
				m=M.getValue();
				canvasUpdate(playingBoard);
				fallingSpeed=40_000_000_0-(speedFactor.getValue()*level*100000000)+1;
				drawBrick(gc,size);
				lineLabel.setText("Line "+line);
				levelLabel.setText("Level "+level);
				scoreLabel.setText("Score "+Math.round(score));
				if(now-l>=fallingSpeed) 
				{
					drawNext(gc2,25);
					if(brick==0) {
						clearNext();
						checkFullRows();
						brick=nextBrick;
						nextBrick=nextBrick();
						addNext();
						if(checkFirstRowFull())
						{
							gc.strokeText("GAME OVER", X/2, Y/2);
							stop();
						}
					}
				
				down();
				l=now;
				}
				
			}
		};
		
		loop.start();
	 	
		playingBoard.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent t)
			{
				count=0;
				p.getChildren().remove(pause);
				loop.start();
			}
		});
		
		
		root.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e)
			{
				if(e.getButton()==MouseButton.PRIMARY)
				{
					left();
				}
				if(e.getButton()==MouseButton.SECONDARY)
				{
					right();
				}
			}
		});
		
		root.setOnScroll(new EventHandler<ScrollEvent>() {
			public void handle(ScrollEvent e)
			{
				
				if(e.getDeltaY()<0)
				{
					flipLeft();
				}
				
				if(e.getDeltaY()>0)
				{
					flipRight();
				}
			}
		});
		
		playingBoard.setOnMouseMoved(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e)
			{
				if(count==0)
				{
					pointInsidePolygon(playingBoard,e,gc,gc2);
				}
			}
		});
		
		playingBoard.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent t)
			{
				p.getChildren().add(pause);
				loop.stop();
				count=0;
			}
		});
	}
	
	public void setRowLimit(int x)
	{
		RowLimit=x;
	}	
	
	public void setColumnLimit(int x)
	{
		ColumnLimit=x;
	}	
	public void setX(int x)
	{
		X=x;
		setY(X);
		setSize(X);
	}
	
	public void setY(int x)
	{
		Y=x*2;
	}
	
	public void setSize(int x)
	{
		double a,b;
		a=Y/(RowLimit+1);
		b=X/(ColumnLimit+1);
		size=(a<b)?a:b;
	}
	
	public void canvasUpdate(Canvas playingBoard)
	{
		playingBoard.setWidth(X);
		playingBoard.setHeight(Y);
	}
	
	public void clearRow(int i){
		for(int j=0;j<=ColumnLimit;j++)
		{
			board[i][j]=0;
		}
		while(i!=0)
		{
			int j=0;
			while(j!=ColumnLimit+1)
			{
				board[i][j]=board[i-1][j];
				j++;
			}
			i--;
		}
	}
	
	public boolean checkFirstRowFull()
	{
			if(board[1][ColumnLimit/2]!=0)
			{
				return true;
			}
		return false;
	}

	void checkFullRows(){
		int i,j,columncount;
		for(i=0;i<=RowLimit;i++)
		{
			columncount=0;
			for(j=0;j<=ColumnLimit;j++)
			{
				if(board[i][j]!=0)
				{
					columncount++;
					if(columncount==ColumnLimit+1)
					{
						rowcount++;
						score+=level*m;
						line++;
						clearRow(i);
						if(rowcount==RowLimit)
						{
							level++;
							rowcount=0;
						}
					}
				}
				else{
				}
			}
		}
		return;		
	}

	public int addBrick(int brickValue)
	{
		switch(brickValue) {
		case 1:
			addZ(ROW-1,COLUMN);
			return 1;
		case 2:
			addO(ROW-1,COLUMN);
			return 2;
		case 3:
			addZInverted(ROW-1,COLUMN);
			return 3;
		case 4:
			addJ(ROW-1,COLUMN);
			return 4;
		case 5:
			addL(ROW-1,COLUMN);
			return 5;
		case 6:
			addT(ROW-1,COLUMN);
			return 6;
		case 7:
			addLineH(ROW-1,COLUMN);
			return 7;
		default:
			return 0;
		}
		
	}
	
	public int nextBrick()
	{
		Random r = new Random();
		int nextBrick= r.nextInt(7)+1;
		return nextBrick;
	}
	
	public void drawNext(GraphicsContext gc,double size)
	{
		int smallBoardX=0,smallBoardY=0;
		gc.setFill(Color.ALICEBLUE);
		gc.fillRect(0,0,size*5,size*5);
		for(smallBoardX=0;smallBoardX<4;smallBoardX++)
		{
			for(smallBoardY=0;smallBoardY<4;smallBoardY++)
			{
				if(smallBoard[smallBoardX][smallBoardY]==0) {
					gc.setFill(Color.WHITE);
					gc.strokeRect(smallBoardY*size, smallBoardX*size, size, size);
					gc.fillRect(smallBoardY*size, smallBoardX*size, size, size);
					
				}
				else if(smallBoard[smallBoardX][smallBoardY]==1)
				{
					gc.setFill(Color.YELLOW);
					gc.strokeRect(smallBoardY*size, smallBoardX*size, size, size);
					gc.fillRect(smallBoardY*size, smallBoardX*size, size, size);
				}
				else if(smallBoard[smallBoardX][smallBoardY]==2)
				{
					gc.setFill(Color.GREEN);
					gc.strokeRect(smallBoardY*size, smallBoardX*size, size, size);
					gc.fillRect(smallBoardY*size, smallBoardX*size, size, size);
				}
				else if(smallBoard[smallBoardX][smallBoardY]==3)
				{
					gc.setFill(Color.BLUE);
					gc.strokeRect(smallBoardY*size, smallBoardX*size, size, size);
					gc.fillRect(smallBoardY*size, smallBoardX*size, size, size);
				}
				else if(smallBoard[smallBoardX][smallBoardY]==4)
				{
					gc.setFill(Color.PURPLE);
					gc.strokeRect(smallBoardY*size, smallBoardX*size, size, size);
					gc.fillRect(smallBoardY*size, smallBoardX*size, size, size);
				}
				else if(smallBoard[smallBoardX][smallBoardY]==5)
				{
					gc.setFill(Color.RED);
					gc.strokeRect(smallBoardY*size, smallBoardX*size, size, size);
					gc.fillRect(smallBoardY*size, smallBoardX*size, size, size);
				}
			}
		}
	}
	
	
	public boolean check(int boardX,int boardY)
	{
		if((boardX<LowerBound))
		{
			return true;
		}
		if(boardX>RowLimit)
		{
			return true;
		}
		if((boardY<0)||(boardY>ColumnLimit))
		{
			return true;
		}
		if(board[boardX][boardY]!=0) {
			return true;
		}
		else return false;
	}
	
	
	public void remove(int boardX,int boardY)
	{
		board[boardX][boardY]=0;
	}
	
	
	public boolean checkZ(int row, int column)
	{
		if(check(row,column)||check(row+1,column+1)||check(row+1,column+2)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;
		}
		return false;
	}
	
	
	public void addZ(int row,int column) {
			addBrick(4,row,column);
			addBrick(4,row,column+1);
			addBrick(4,row+1,column+1);
			addBrick(4,row+1,column+2);
	}
	
	public void removeZ(int row,int column) {
			remove(row,column);
			remove(row,column+1);
			remove(row+1,column+1);
			remove(row+1,column+2);	
	}
	
	public boolean checkZ2(int row, int column)
	{
		if(check(row+1,column)||check(row+2,column-1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;
		}
		return false;
	}
	
	public void addZ2(int row,int column) {
			addBrick(4,row,column);
			addBrick(4,row+1,column);
			addBrick(4,row+1,column-1);
			addBrick(4,row+2,column-1);
	}
	
	public void removeZ2(int row,int column) {
			remove(row,column);
			remove(row+1,column);
			remove(row+1,column-1);
			remove(row+2,column-1);	
	}
	
	
	public boolean checkO(int row,int column)
	{
		if(check(row+1,column)||check(row+1,column+1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;
		}
		return false;
	}
	
	
	public void addO(int row,int column) {
			addBrick(2,row,column);
			addBrick(2,row,column+1);
			addBrick(2,row+1,column);
			addBrick(2,row+1,column+1);
	}
	
	
	public void removeO(int row,int column) {
			remove(row,column);
			remove(row+1,column);
			remove(row,column+1);
			remove(row+1,column+1);
	}
	
	public boolean checkZInverted(int row,int column)
	{
		if(check(row,column)||check(row+1,column-1)||check(row+1,column-2)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;
		}
		return false;
	}

	public void addZInverted(int row,int column) {
			addBrick(1,row,column);
			addBrick(1,row,column-1);
			addBrick(1,row+1,column-1);
			addBrick(1,row+1,column-2);		
	}
	
	public void removeZInverted(int row,int column) {
		remove(row,column);
		remove(row,column-1);
		remove(row+1,column-1);
		remove(row+1,column-2);
}
	
	public boolean checkZInverted2(int row, int column)
	{
		if(check(row+1,column)||check(row+2,column+1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;
		}
		return false;
	}
	
	public void addZInverted2(int row,int column) {
			addBrick(1,row,column);
			addBrick(1,row+1,column);
			addBrick(1,row+1,column+1);
			addBrick(1,row+2,column+1);		
	}
	
	public void removeZInverted2(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+1,column+1);
		remove(row+2,column+1);
}

	public boolean checkJ(int row,int column)
	{
		if(check(row+2,column)||check(row+2,column-1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addJ(int row,int column) {
			addBrick(3,row,column);
			addBrick(3,row+1,column);
			addBrick(3,row+2,column);
			addBrick(3,row+2,column-1);
	}
	
	public void removeJ(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+2,column);
		remove(row+2,column-1);
}
	
	public boolean checkJ1(int row,int column)
	{
		if(check(row+1,column)||check(row+1,column+1)||check(row+1,column+2)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addJ1(int row,int column) {
			addBrick(3,row,column);
			addBrick(3,row+1,column);
			addBrick(3,row+1,column+1);
			addBrick(3,row+1,column+2);
	}
	
	public void removeJ1(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+1,column+1);
		remove(row+1,column+2);
}
	
	public boolean checkJ2(int row,int column)
	{
		if(check(row,column+1)||check(row+2,column)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addJ2(int row,int column) {
			addBrick(3,row,column);
			addBrick(3,row,column+1);
			addBrick(3,row+1,column);
			addBrick(3,row+2,column);
	}
	
	public void removeJ2(int row,int column) {
		remove(row,column);
		remove(row,column+1);
		remove(row+1,column);
		remove(row+2,column);
}
	
	public boolean checkJ3(int row,int column)
	{
		if(check(row,column-1)||check(row+1,column)||check(row,column-2)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addJ3(int row,int column) {
			addBrick(3,row,column);
			addBrick(3,row+1,column);
			addBrick(3,row,column-1);
			addBrick(3,row,column-2);
	}
	
	public void removeJ3(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row,column-1);
		remove(row,column-2);
}
	
	public boolean checkL(int row,int column)
	{
		if(check(row+2,column)||check(row+2,column+1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addL(int row,int column) {
			addBrick(5,row,column);
			addBrick(5,row+1,column);
			addBrick(5,row+2,column);
			addBrick(5,row+2,column+1);
	}
	
	public void removeL(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+2,column);
		remove(row+2,column+1);
}
	
	public boolean checkL1(int row,int column)
	{
		if(check(row+1,column)||check(row+1,column-1)||check(row+1,column-2)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addL1(int row,int column) {
			addBrick(5,row,column);
			addBrick(5,row+1,column);
			addBrick(5,row+1,column-1);
			addBrick(5,row+1,column-2);
	}
	
	public void removeL1(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+1,column-1);
		remove(row+1,column-2);
}
	
	
	public boolean checkL2(int row,int column)
	{
		if(check(row,column-1)||check(row+2,column)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addL2(int row,int column) {
			addBrick(5,row,column);
			addBrick(5,row+1,column);
			addBrick(5,row+2,column);
			addBrick(5,row,column-1);
	}
	
	public void removeL2(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+2,column);
		remove(row,column-1);
}

	
	public boolean checkL3(int row,int column)
	{
		if(check(row+1,column)||check(row,column+1)||check(row,column+2)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addL3(int row,int column) {
			addBrick(5,row,column);
			addBrick(5,row+1,column);
			addBrick(5,row,column+1);
			addBrick(5,row,column+2);
	}
	
	public void removeL3(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row,column+1);
		remove(row,column+2);
}

	
	public boolean checkT(int row,int column)
	{
		if(check(row+1,column)||check(row,column-1)||check(row,column+1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addT(int row,int column) {
			addBrick(1,row,column);
			addBrick(1,row+1,column);
			addBrick(1,row,column-1);
			addBrick(1,row,column+1);
	}
	
	public void removeT(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row,column-1);
		remove(row,column+1);
}

	public boolean checkTL(int row,int column)
	{
		if(check(row+2,column)||check(row+1,column-1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addTL(int row,int column) {
			addBrick(1,row,column);
			addBrick(1,row+1,column);
			addBrick(1,row+1,column-1);
			addBrick(1,row+2,column);
	}
	
	public void removeTL(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+1,column-1);
		remove(row+2,column);
}
	
	public boolean checkTR(int row,int column)
	{
		if(check(row+2,column)||check(row+1,column+1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addTR(int row,int column) {
			addBrick(1,row,column);
			addBrick(1,row+1,column);
			addBrick(1,row+1,column+1);
			addBrick(1,row+2,column);
	}
	
	public void removeTR(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+1,column+1);
		remove(row+2,column);
}
	
	public boolean checkTD(int row,int column)
	{
		if(check(row+1,column)||check(row+1,column+1)||check(row+1,column-1)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addTD(int row,int column) {
			addBrick(1,row,column);
			addBrick(1,row+1,column);
			addBrick(1,row+1,column+1);
			addBrick(1,row+1,column-1);
	}
	
	public void removeTD(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+1,column+1);
		remove(row+1,column-1);
}

	public boolean checkLineH(int row,int column)
	{
		if(check(row,column+1)||check(row,column+2)||check(row,column+3)||check(row,column)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addLineH(int row,int column) {
			addBrick(3,row,column);
			addBrick(3,row,column+1);
			addBrick(3,row,column+2);
			addBrick(3,row,column+3);
	}
	
	public void removeLineH(int row,int column) {
		remove(row,column);
		remove(row,column+1);
		remove(row,column+2);
		remove(row,column+3);
}
	
	public boolean checkLineV(int row,int column)
	{
		if(check(row+3,column)) {
			brick=0;
			ROW=-1;
			COLUMN=ColumnLimit/2;
			return true;	
		}
		return false;
	}

	public void addLineV(int row,int column) {
			addBrick(3,row,column);
			addBrick(3,row+1,column);
			addBrick(3,row+2,column);
			addBrick(3,row+3,column);
	}
	
	public void removeLineV(int row,int column) {
		remove(row,column);
		remove(row+1,column);
		remove(row+2,column);
		remove(row+3,column);
}
	public void down()
	{
		if(brick==1)
		{
			if(checkZ(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeZ(ROW-1,COLUMN);
				addZ(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==2)
		{
			if(checkO(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeO(ROW-1,COLUMN);
				addO(ROW,COLUMN);
				}
			ROW++;
		}
		
		
		if(brick==3)
		{
			if(checkZInverted(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeZInverted(ROW-1,COLUMN);
				addZInverted(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==4)
		{
			if(checkJ(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeJ(ROW-1,COLUMN);
				addJ(ROW,COLUMN);
				}
			ROW++;
		}
		if(brick==5)
		{
			if(checkL(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeL(ROW-1,COLUMN);
				addL(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==6)
		{
			if(checkT(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeT(ROW-1,COLUMN);
				addT(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==21)
		{
			if(checkZ2(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeZ2(ROW-1,COLUMN);
				addZ2(ROW,COLUMN);
				}
			ROW++;
		}
		if(brick==22)
		{
			if(checkZInverted2(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeZInverted2(ROW-1,COLUMN);
				addZInverted2(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==8)
		{
			if(checkTL(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeTL(ROW-1,COLUMN);
				addTL(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==9)
		{
			if(checkTR(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeTR(ROW-1,COLUMN);
				addTR(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==10)
		{
			if(checkTD(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeTD(ROW-1,COLUMN);
				addTD(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==11)
		{
			if(checkJ1(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeJ1(ROW-1,COLUMN);
				addJ1(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==12)
		{
			if(checkJ2(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeJ2(ROW-1,COLUMN);
				addJ2(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==13)
		{
			if(checkJ3(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeJ3(ROW-1,COLUMN);
				addJ3(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==14)
		{
			if(checkL1(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeL1(ROW-1,COLUMN);
				addL1(ROW,COLUMN);
				}
			ROW++;
		}
		
		if(brick==15)
		{
			if(checkL2(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeL2(ROW-1,COLUMN);
				addL2(ROW,COLUMN);
				}
			ROW++;
		}
		if(brick==16)
		{
			if(checkL3(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeL3(ROW-1,COLUMN);
				addL3(ROW,COLUMN);
				}
			ROW++;
		}
		if(brick==7)
		{
			if(checkLineH(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeLineH(ROW-1,COLUMN);
				addLineH(ROW,COLUMN);
				}
			ROW++;
		}
		if(brick==17)
		{
			if(checkLineV(ROW,COLUMN))
			{
			}
			else 
				{if(ROW>0)
					removeLineV(ROW-1,COLUMN);
				addLineV(ROW,COLUMN);
				}
			ROW++;
		}
	}

	
	public void flipLeft()
	{
		if(brick==1)
		{
			if(check(ROW+1,COLUMN)||check(ROW,COLUMN)) {
				return;
			}
			else {
				removeZ(ROW-1,COLUMN);
				COLUMN++;
				addZ2(ROW-1,COLUMN);
				brick=21;
			}
		}
		
		else if(brick==21)//Z2
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN+1)) {
				return;
			}
			else {
				removeZ2(ROW-1,COLUMN);
				COLUMN--;
				addZ(ROW-1,COLUMN);
				brick=1;
			}
		}
		
		if(brick==3)
		{
			if(check(ROW,COLUMN)||check(ROW+1,COLUMN)) {
				return;
			}
			else {
				removeZInverted(ROW-1,COLUMN);
				COLUMN--;
				addZInverted2(ROW-1,COLUMN);
				brick=22;
			}
		}

		else if(brick==22)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN-1)) {
				return;
			}
			else {
				removeZInverted2(ROW-1,COLUMN);
				COLUMN++;
				addZInverted(ROW-1,COLUMN);
				brick=3;
			}
		}
		
		else if(brick==6)
		{
			if(check(ROW+1,COLUMN)||check(ROW,COLUMN-1)) {
				return;
			}
			else {
				removeT(ROW-1,COLUMN);
				addTL(ROW-1,COLUMN);
				brick=8;
			}
		}
		
		else if(brick==8)
		{
			if(check(ROW,COLUMN+1)) {
				return;
			}
			else {
				removeTL(ROW-1,COLUMN);
				addTD(ROW-1,COLUMN);
				brick=10;
			}
		}
		
		else if(brick==10)
		{
			if(check(ROW+1,COLUMN)) {
				return;
			}
			else {
				removeTD(ROW-1,COLUMN);
				addTR(ROW-1,COLUMN);
				brick=9;
			}
		}
		
		else if(brick==9)
		{
			if(check(ROW,COLUMN-1)) {
				return;
			}
			else {
				removeTR(ROW-1,COLUMN);
				addT(ROW-1,COLUMN);
				brick=6;
			}
		}
		
		else if(brick==11)
		{
			if(check(ROW+1,COLUMN)||check(ROW-1,COLUMN+1)) {
				return;
			}
			else {
				removeJ1(ROW-1,COLUMN);
				addJ2(ROW-1,COLUMN);
				brick=12;
			}
		}
		
		else if(brick==12)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW-1,COLUMN-2)) {
				return;
			}
			else {
				removeJ2(ROW-1,COLUMN);
				addJ3(ROW-1,COLUMN);
				brick=13;
			}
		}
		else if(brick==13)
		{
			if(check(ROW+1,COLUMN)||check(ROW+1,COLUMN-1)) {
				return;
			}
			else {
				removeJ3(ROW-1,COLUMN);
				addJ(ROW-1,COLUMN);
				brick=4;
			}
		}
		else if(brick==4)
		{
			if(check(ROW,COLUMN+1)||check(ROW,COLUMN+2)) {
				return;
			}
			else {
				removeJ(ROW-1,COLUMN);
				addJ1(ROW-1,COLUMN);
				brick=11;
			}
		}
		
		else if(brick==5)
		{
			if(check(ROW,COLUMN-1)||check(ROW,COLUMN-2)) {
				return;
			}
			else {
				removeL(ROW-1,COLUMN);
				addL1(ROW-1,COLUMN);
				brick=14;
			}
		}
		
		else if(brick==14)
		{
			if(check(ROW+1,COLUMN)||check(ROW-1,COLUMN-1)) {
				return;
			}
			else {
				removeL1(ROW-1,COLUMN);
				addL2(ROW-1,COLUMN);
				brick=15;
			}
		}
		
		else if(brick==15)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW-1,COLUMN+2)) {
				return;
			}
			else {
				removeL2(ROW-1,COLUMN);
				addL3(ROW-1,COLUMN);
				brick=16;
			}
		}
		
		else if(brick==16)
		{
			if(check(ROW+1,COLUMN)||check(ROW+1,COLUMN+1)) {
				return;
			}
			else {
				removeL3(ROW-1,COLUMN);
				addL(ROW-1,COLUMN);
				brick=5;
			}
		}
		else if(brick==7)
		{
			if(check(ROW,COLUMN)||check(ROW+1,COLUMN)||check(ROW+2,COLUMN)) {
				return;
			}
			else {
				removeLineH(ROW-1,COLUMN);
				addLineV(ROW-1,COLUMN);
				brick=17;
			}
		}
		else if(brick==17)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW-1,COLUMN+2)||check(ROW-1,COLUMN+3)) {
				return;
			}
			else {
				removeLineV(ROW-1,COLUMN);
				addLineH(ROW-1,COLUMN);
				brick=7;
			}
		}
	}
	
	public void flipRight()
	{
		if(brick==1)
		{
			if(check(ROW+1,COLUMN)||check(ROW,COLUMN)) {
				return;
			}
			else {
				removeZ(ROW-1,COLUMN);
				COLUMN++;
				addZ2(ROW-1,COLUMN);
				brick=21;
			}
		}
		
		else if(brick==21)//Z2
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN+1)) {
				return;
			}
			else {
				removeZ2(ROW-1,COLUMN);
				COLUMN--;
				addZ(ROW-1,COLUMN);
				brick=1;
			}
		}
		
		if(brick==3)
		{
			if(check(ROW,COLUMN)||check(ROW+1,COLUMN)) {
				return;
			}
			else {
				removeZInverted(ROW-1,COLUMN);
				COLUMN--;
				addZInverted2(ROW-1,COLUMN);
				brick=22;
			}
		}

		else if(brick==22)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN-1)) {
				return;
			}
			else {
				removeZInverted2(ROW-1,COLUMN);
				COLUMN++;
				addZInverted(ROW-1,COLUMN);
				brick=3;
			}
		}
		
		else if(brick==6)
		{
			if(check(ROW+1,COLUMN)||check(ROW,COLUMN+1)) {
				return;
			}
			else {
				removeT(ROW-1,COLUMN);
				addTR(ROW-1,COLUMN);
				brick=9;
			}
		}
		
		else if(brick==8)
		{
			if(check(ROW,COLUMN+1)) {
				return;
			}
			else {
				removeTL(ROW-1,COLUMN);
				addT(ROW-1,COLUMN);
				brick=6;
			}
		}
		
		else if(brick==10)
		{
			if(check(ROW+1,COLUMN)) {
				return;
			}
			else {
				removeTD(ROW-1,COLUMN);
				addTL(ROW-1,COLUMN);
				brick=8;
			}
		}
		
		else if(brick==9)
		{
			if(check(ROW,COLUMN-1)) {
				return;
			}
			else {
				removeTR(ROW-1,COLUMN);
				addTD(ROW-1,COLUMN);
				brick=10;
			}
		}
		
		else if(brick==11)
		{
			if(check(ROW+1,COLUMN)||check(ROW+1,COLUMN-1)) {
				return;
			}
			else {
				removeJ1(ROW-1,COLUMN);
				addJ(ROW-1,COLUMN);
				brick=4;
			}
		}
		
		else if(brick==12)
		{
			if(check(ROW,COLUMN+1)||check(ROW,COLUMN+2)) {
				return;
			}
			else {
				removeJ2(ROW-1,COLUMN);
				addJ1(ROW-1,COLUMN);
				brick=11;
			}
		}
		else if(brick==13)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW+1,COLUMN)) {
				return;
			}
			else {
				removeJ3(ROW-1,COLUMN);
				addJ2(ROW-1,COLUMN);
				brick=12;
			}
		}
		else if(brick==4)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW-1,COLUMN-2)) {
				return;
			}
			else {
				removeJ(ROW-1,COLUMN);
				addJ3(ROW-1,COLUMN);
				brick=13;
			}
		}
		
		else if(brick==5)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW-1,COLUMN+2)) {
				return;
			}
			else {
				removeL(ROW-1,COLUMN);
				addL3(ROW-1,COLUMN);
				brick=16;
			}
		}
		
		else if(brick==14)
		{
			if(check(ROW+1,COLUMN)||check(ROW+1,COLUMN+1)) {
				return;
			}
			else {
				removeL1(ROW-1,COLUMN);
				addL(ROW-1,COLUMN);
				brick=5;
			}
		}
		
		else if(brick==15)
		{
			if(check(ROW,COLUMN+1)||check(ROW,COLUMN-2)) {
				return;
			}
			else {
				removeL2(ROW-1,COLUMN);
				addL1(ROW-1,COLUMN);
				brick=14;
			}
		}
		
		else if(brick==16)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW+1,COLUMN)) {
				return;
			}
			else {
				removeL3(ROW-1,COLUMN);
				addL2(ROW-1,COLUMN);
				brick=15;
			}
		}
		else if(brick==7)
		{
			if(check(ROW+1,COLUMN+3)||check(ROW+2,COLUMN+3)||check(ROW,COLUMN+3)) {
				return;
			}
			else {
				removeLineH(ROW-1,COLUMN);
				COLUMN=COLUMN+3;
				addLineV(ROW-1,COLUMN);
				brick=17;
			}
		}
		else if(brick==17)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW-1,COLUMN-2)||check(ROW-1,COLUMN-3)) {
				return;
			}
			else {
				removeLineV(ROW-1,COLUMN);
				COLUMN=COLUMN-3;
				addLineH(ROW-1,COLUMN);
				brick=7;
			}
		}
	}
	
	
	public void drawBrick(GraphicsContext gc,double size)
	{
		int boardX=0,boardY=0;
		gc.setFill(Color.WHITE);
		gc.fillRect(0,0,X+50,Y+50);
		for(boardX=0;boardX<RowLimit+1;boardX++)
		{
			gc.setStroke(Color.BLACK);
			for(boardY=0;boardY<ColumnLimit+1;boardY++)
			{
				if(board[boardX][boardY]==0) {
					gc.setFill(Color.WHITE);
					gc.strokeRect(boardY*size, boardX*size, size, size);
					gc.fillRect(boardY*size, boardX*size, size, size);
				}
				else if(board[boardX][boardY]==1)
				{
					gc.setFill(Color.YELLOW);
					gc.strokeRect(boardY*size, boardX*size, size, size);
					gc.fillRect(boardY*size, boardX*size, size, size);
				}
				else if(board[boardX][boardY]==2)
				{
					gc.setFill(Color.GREEN);
					gc.strokeRect(boardY*size, boardX*size, size, size);
					gc.fillRect(boardY*size, boardX*size, size, size);
				}
				else if(board[boardX][boardY]==3)
				{
					gc.setFill(Color.BLUE);
					gc.strokeRect(boardY*size, boardX*size, size, size);
					gc.fillRect(boardY*size, boardX*size, size, size);
				}
				else if(board[boardX][boardY]==4)
				{
					gc.setFill(Color.PURPLE);
					gc.strokeRect(boardY*size, boardX*size, size, size);
					gc.fillRect(boardY*size, boardX*size, size, size);
				}
				else if(board[boardX][boardY]==5)
				{
					gc.setFill(Color.RED);
					gc.strokeRect(boardY*size, boardX*size, size, size);
					gc.fillRect(boardY*size, boardX*size, size, size);
				}
			}
		}
		gc.strokeRect(0, 0, X, Y);
	}
	
	
	public void left()
	{
		if(brick==1)
		{
			if((COLUMN-1)<0)
			{
				
			}
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN))
			{
				
			}
			else {
				removeZ(ROW-1,COLUMN);
				COLUMN--;
				addZ(ROW-1,COLUMN);
			}
		}
		
		if(brick==2)
		{
			if((COLUMN-1)<0)
			{
				
			}
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1))
			{
				
			}
			else {
				removeO(ROW-1,COLUMN);
				COLUMN--;
				addO(ROW-1,COLUMN);
			}
		}
		
		
		if(brick==3)
		{
			if((COLUMN-3)<0)
			{
				
			}
			if(check(ROW-1,COLUMN-2)||check(ROW,COLUMN-3))
			{
				
			}
			else {
				removeZInverted(ROW-1,COLUMN);
				COLUMN--;
				addZInverted(ROW-1,COLUMN);
			}
		}
		
		if(brick==4)
		{
			if((COLUMN-2)<0)
			{
				
			}
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1)||check(ROW+1,COLUMN-2))
			{
				
			}
			else {
				removeJ(ROW-1,COLUMN);
				COLUMN--;
				addJ(ROW-1,COLUMN);
			}
		}
		
		
		if(brick==5)
		{
			if((COLUMN-1)<0)
			{
				
			}
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1)||check(ROW+1,COLUMN-1))
			{
				
			}
			else {
				removeL(ROW-1,COLUMN);
				COLUMN--;
				addL(ROW-1,COLUMN);
			}
		}
		
		if(brick==6)
		{
			if((COLUMN-2)<0)
			{
				
			}
			if(check(ROW-1,COLUMN-2)||check(ROW,COLUMN-1))
			{
				
			}
			else {
				removeT(ROW-1,COLUMN);
				COLUMN--;
				addT(ROW-1,COLUMN);
			}
		}
		
		if(brick==21)
		{
			if(check(ROW,COLUMN-2)||check(ROW+1,COLUMN-2))
			{
				
			}
			else {
				removeZ2(ROW-1,COLUMN);
				COLUMN--;
				addZ2(ROW-1,COLUMN);
			}
		}
		
		if(brick==22)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1))
			{
				
			}
			else {
				removeZInverted2(ROW-1,COLUMN);
				COLUMN--;
				addZInverted2(ROW-1,COLUMN);
			}
		}
		
		if(brick==10)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-2))
			{
				
			}
			else {
				removeTD(ROW-1,COLUMN);
				COLUMN--;
				addTD(ROW-1,COLUMN);
			}
		}
		if(brick==8)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-2)||check(ROW+1,COLUMN-1))
			{
				
			}
			else
			{
				removeTL(ROW-1,COLUMN);
				COLUMN--;
				addTL(ROW-1,COLUMN);
			}
		}
		if(brick==9)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1)||check(ROW+1,COLUMN-1))
			{
				
			}
			else
			{
				removeTR(ROW-1,COLUMN);
				COLUMN--;
				addTR(ROW-1,COLUMN);
			}
		}
		
		if(brick==11)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1))
			{
				
			}
			else
			{
				removeJ1(ROW-1,COLUMN);
				COLUMN--;
				addJ1(ROW-1,COLUMN);
			}
		}
		if(brick==12)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1)||check(ROW+1,COLUMN-1))
			{
				
			}
			else
			{
				removeJ2(ROW-1,COLUMN);
				COLUMN--;
				addJ2(ROW-1,COLUMN);
			}
		}
		if(brick==13)
		{
			if(check(ROW-1,COLUMN-3)||check(ROW,COLUMN-1))
			{
				
			}
			else
			{
				removeJ3(ROW-1,COLUMN);
				COLUMN--;
				addJ3(ROW-1,COLUMN);
			}
		}
		if(brick==14)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-3))
			{
				
			}
			else
			{
				removeL1(ROW-1,COLUMN);
				COLUMN--;
				addL1(ROW-1,COLUMN);
			}
		}
		
		if(brick==15)
		{
			if(check(ROW-1,COLUMN-2)||check(ROW,COLUMN-1)||check(ROW+1,COLUMN-1))
			{
				
			}
			else
			{
				removeL2(ROW-1,COLUMN);
				COLUMN--;
				addL2(ROW-1,COLUMN);
			}
		}
		if(brick==16)
		{
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1))
			{
				
			}
			else
			{
				removeL3(ROW-1,COLUMN);
				COLUMN--;
				addL3(ROW-1,COLUMN);
			}
		}
		if(brick==7)
		{
			if(check(ROW-1,COLUMN-1))
			{
				
			}
			else
			{
				removeLineH(ROW-1,COLUMN);
				COLUMN--;
				addLineH(ROW-1,COLUMN);
			}
		}
		if(brick==17) {
			if(check(ROW-1,COLUMN-1)||check(ROW,COLUMN-1)||check(ROW+1,COLUMN-1)||check(ROW+2,COLUMN-1))
			{
				
			}
			else
			{
				removeLineV(ROW-1,COLUMN);
				COLUMN--;
				addLineV(ROW-1,COLUMN);
			}
		}
	}
	
	
	public void right()
	{
		if(brick==1)
		{
			if((COLUMN+3)>9)
			{
				
			}
			if(check(ROW-1,COLUMN+2)||check(ROW,COLUMN+3))
			{
				
			}
			else {
				removeZ(ROW-1,COLUMN);
				COLUMN++;
				addZ(ROW-1,COLUMN);
			}
		}
		
		if(brick==2)
		{
			if((COLUMN+3)>9)
			{
				
			}
			if(check(ROW-1,COLUMN+2)||check(ROW,COLUMN+2))
			{
				
			}
			else {
				removeO(ROW-1,COLUMN);
				COLUMN++;
				addO(ROW-1,COLUMN);
			}
		}
		
		
		if(brick==3)
		{
			if((COLUMN+1)>9)
			{
				
			}
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN))
			{
				
			}
			else {
				removeZInverted(ROW-1,COLUMN);
				COLUMN++;
				addZInverted(ROW-1,COLUMN);
			}
		}
		
		if(brick==4)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN+1)||check(ROW+1,COLUMN+1))
			{
				
			}
			else {
				removeJ(ROW-1,COLUMN);
				COLUMN++;
				addJ(ROW-1,COLUMN);
			}
		}
		
		if(brick==5)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN+1)||check(ROW+1,COLUMN+2))
			{
				
			}
			else {
				removeL(ROW-1,COLUMN);
				COLUMN++;
				addL(ROW-1,COLUMN);
			}
		}
		
		if(brick==6)
		{
			if(check(ROW-1,COLUMN+2)||check(ROW,COLUMN+1))
			{
				
			}
			else {
				removeT(ROW-1,COLUMN);
				COLUMN++;
				addT(ROW-1,COLUMN);
			}
		}
		
		if(brick==21)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN+1))
			{
				
			}
			else {
				removeZ2(ROW-1,COLUMN);
				COLUMN++;
				addZ2(ROW-1,COLUMN);
			}
		}
		
		if(brick==22)
		{
			if(check(ROW,COLUMN+2)||check(ROW+1,COLUMN+2))
			{
				
			}
			else {
				removeZInverted2(ROW-1,COLUMN);
				COLUMN++;
				addZInverted2(ROW-1,COLUMN);
			}
		}
		
		if(brick==8)
		{
			if(check(ROW,COLUMN+1)||check(ROW+1,COLUMN+1)||check(ROW-1,COLUMN+1))
			{
				
			}
			else {
				removeTL(ROW-1,COLUMN);
				COLUMN++;
				addTL(ROW-1,COLUMN);
			}
		}
		
		if(brick==9)
		{
			if(check(ROW,COLUMN+2)||check(ROW+1,COLUMN+1)||check(ROW-1,COLUMN+1))
			{
				
			}
			else {
				removeTR(ROW-1,COLUMN);
				COLUMN++;
				addTR(ROW-1,COLUMN);
			}
		}
		
		if(brick==10)
		{
			if(check(ROW,COLUMN+2)||check(ROW-1,COLUMN+1))
			{
				
			}
			else {
				removeTD(ROW-1,COLUMN);
				COLUMN++;
				addTD(ROW-1,COLUMN);
			}
		}
		
		if(brick==11)
		{
			if(check(ROW,COLUMN+3)||check(ROW-1,COLUMN+1))
			{
				
			}
			else {
				removeJ1(ROW-1,COLUMN);
				COLUMN++;
				addJ1(ROW-1,COLUMN);
			}
		}
		
		if(brick==12)
		{
			if(check(ROW,COLUMN+1)||check(ROW-1,COLUMN+2)||check(ROW+1,COLUMN+1))
			{
				
			}
			else {
				removeJ2(ROW-1,COLUMN);
				COLUMN++;
				addJ2(ROW-1,COLUMN);
			}
		}
		if(brick==13)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN+1))
			{
				
			}
			else {
				removeJ3(ROW-1,COLUMN);
				COLUMN++;
				addJ3(ROW-1,COLUMN);
			}
		}
		if(brick==14)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN+1))
			{
				
			}
			else {
				removeL1(ROW-1,COLUMN);
				COLUMN++;
				addL1(ROW-1,COLUMN);
			}
		}
		if(brick==15)
		{
			if(check(ROW-1,COLUMN+1)||check(ROW,COLUMN+1)||check(ROW+1,COLUMN+1))
			{
				
			}
			else {
				removeL2(ROW-1,COLUMN);
				COLUMN++;
				addL2(ROW-1,COLUMN);
			}
		}
		if(brick==16)
		{
			if(check(ROW-1,COLUMN+3)||check(ROW,COLUMN+1))
			{
				
			}
			else {
				removeL3(ROW-1,COLUMN);
				COLUMN++;
				addL3(ROW-1,COLUMN);
			}
		}
		if(brick==7)
		{
			if(check(ROW-1,COLUMN+4))
			{
				
			}
			else {
				removeLineH(ROW-1,COLUMN);
				COLUMN++;
				addLineH(ROW-1,COLUMN);
			}
		}
		if(brick==17)
		{
			if(check(ROW,COLUMN+1)||check(ROW-1,COLUMN+1)||check(ROW+2,COLUMN+1)||check(ROW+3,COLUMN+1))
			{
				
			}
			else {
				removeLineV(ROW-1,COLUMN);
				COLUMN++;
				addLineV(ROW-1,COLUMN);
			}
		}
	}
	
	public void addNextT(int row,int column) {
		addNextBrick(1,row,column);
		addNextBrick(1,row,column-1);
		addNextBrick(1,row+1,column);
		addNextBrick(1,row,column+1);
}
	public void addNextZ(int row,int column) {
		addNextBrick(4,row,column);
		addNextBrick(4,row,column+1);
		addNextBrick(4,row+1,column+1);
		addNextBrick(4,row+1,column+2);
}
	public void addNextZInverted(int row,int column) {
		addNextBrick(1,row,column);
		addNextBrick(1,row,column-1);
		addNextBrick(1,row+1,column-1);
		addNextBrick(1,row+1,column-2);		
}
	public void addNextO(int row,int column) {
		addNextBrick(2,row,column);
		addNextBrick(2,row,column+1);
		addNextBrick(2,row+1,column);
		addNextBrick(2,row+1,column+1);
}

	public void addNextL(int row,int column) {
		addNextBrick(5,row,column);
		addNextBrick(5,row+1,column);
		addNextBrick(5,row+2,column);
		addNextBrick(5,row+2,column+1);
}
	public void addNextJ(int row,int column) {
		addNextBrick(3,row,column);
		addNextBrick(3,row+1,column);
		addNextBrick(3,row+2,column);
		addNextBrick(3,row+2,column-1);
}
	public void addNextLine(int row,int column) {
		addNextBrick(3,row,column);
		addNextBrick(3,row,column+1);
		addNextBrick(3,row,column+2);
		addNextBrick(3,row,column+3);
}
	
	public void addNext()
	{
		if(nextBrick==1)
		{
			addNextZ(0,0);
		}
		if(nextBrick==2)
		{
			addNextO(0,0);
		}
		if(nextBrick==3)
		{
			addNextZInverted(0,3);
		}
		if(nextBrick==4)
		{
			addNextJ(0,3);
		}
		if(nextBrick==5)
		{
			addNextL(0,2);
		}
		if(nextBrick==6)
		{
			addNextT(0,1);
		}
		if(nextBrick==7)
		{
			addNextLine(1,0);
		}
	}

	public void addBrick(int type,int boardX,int boardY)
	{
		board[boardX][boardY]=type;
	}
	
	public void addNextBrick(int type,int boardX,int boardY)
	{
		smallBoard[boardX][boardY]=type;
	}
	
	public void clearNext() {
		int smallBoardX=0,smallBoardY=0;
		for(smallBoardX=0;smallBoardX<4;smallBoardX++)
		{
			for(smallBoardY=0;smallBoardY<4;smallBoardY++)
			{
				smallBoard[smallBoardX][smallBoardY]=0;
			}
		}
	}

	public void pointInsidePolygon(Canvas playingBoard,MouseEvent e,GraphicsContext gc,GraphicsContext gc2)
	{
		int row=ROW,column=COLUMN;
		WritableImage i = playingBoard.snapshot(new SnapshotParameters(),null);
		PixelReader r = i.getPixelReader();
		Color w =r.getColor((int)e.getX(), (int)e.getY());
		//System.out.println(r.getColor((int)e.getX(), (int)e.getY()));
		//System.out.println(brick);
		double rowsize=(ROW-1)*size;
		double columnsize=COLUMN*size;
		if(!w.equals(Color.WHITE)&&(!w.equals(Color.GREY)))
		{
			switch(brick)
			{
			case 1:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+2*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 2:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+2*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 3:
				if(columnsize-size*2<=e.getX()&&e.getX()<=columnsize+size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 4:
				if(columnsize-size<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 5:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+2*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 6:
				if(columnsize-size<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 7:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+4*size&&rowsize<=e.getY()&&e.getY()<=rowsize+2*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 8:
				if(columnsize-size<=e.getX()&&e.getX()<=columnsize+2*size&rowsize<=e.getY()&&e.getY()<=rowsize+size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 9:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 10:
				if(columnsize-size<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+2*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 11:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 12:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+2*size&&rowsize<=e.getY()&&e.getY()<=rowsize+2*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 13:
				if(columnsize-2*size<=e.getX()&&e.getX()<=columnsize+size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 14:
				if(columnsize-4*size<=e.getX()&&e.getX()<=columnsize+size&&rowsize<=e.getY()&&e.getY()<=rowsize+2*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 15:
				if(columnsize-size<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 16:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 17:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+size&&rowsize<=e.getY()&&e.getY()<=rowsize+4*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 21:
				if(columnsize-2*size<=e.getX()&&e.getX()<=columnsize+size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			case 22:
				if(columnsize<=e.getX()&&e.getX()<=columnsize+3*size&&rowsize<=e.getY()&&e.getY()<=rowsize+3*size)
				{
					switchShape(row,column,gc,gc2);
				}
				return;
			}
			
		}
	}
	
	public boolean checkBrick(int brick,int ROW,int COLUMN)
	{
		if(brick==1)
		{
			return checkZ(ROW,COLUMN);
		}
		else if(brick==2)
		{
			return checkO(ROW,COLUMN);
		}
		else if(brick==3)
		{
			return checkZInverted(ROW,COLUMN);
		}
		else if(brick==4)
		{
			return checkJ(ROW,COLUMN);
		}
		else if(brick==5)
		{
			return checkL(ROW,COLUMN);
		}
		else if(brick==6)
		{
			return checkT(ROW,COLUMN);
		}
		else if(brick==7)
		{
			return checkLineH(ROW,COLUMN);
		}
		else if(brick==8)
		{
			return checkTL(ROW,COLUMN);
		}
		else if(brick==9)
		{
			return checkTR(ROW,COLUMN);
		}
		else if(brick==10)
		{
			return checkTD(ROW,COLUMN);
		}
		else if(brick==11)
		{
			return checkJ1(ROW,COLUMN);
		}
		else if(brick==12)
		{
			return checkJ2(ROW,COLUMN);
		}
		else if(brick==13)
		{
			return checkJ3(ROW,COLUMN);
		}
		else if(brick==14)
		{
			return checkL1(ROW,COLUMN);
		}
		else if(brick==15)
		{
			return checkL2(ROW,COLUMN);
		}
		else if(brick==16)
		{
			return checkL3(ROW,COLUMN);
		}
		else if(brick==17)
		{
			return checkLineV(ROW,COLUMN);
		}
		else if(brick==21)
		{
			return checkZ2(ROW,COLUMN);
		}
		else if(brick==22)
		{
			return checkZInverted2(ROW,COLUMN);
		}
		return false;
	}
	
	public void removeBrick(int brick,int ROW,int COLUMN)
	{
		if(ROW>RowLimit)
		{
			return;
		}
		if(ROW<LowerBound)
		{
			return;
		}
		if(COLUMN>ColumnLimit)
		{
			return;
		}
		if(COLUMN<LowerBound)
		{
			return;
		}
		else if(brick==1)
		{
			removeZ(ROW,COLUMN);
		}
		else if(brick==2)
		{
			removeO(ROW,COLUMN);
		}
		
		else if(brick==3)
		{
			removeZInverted(ROW,COLUMN);
		}
		else if(brick==4)
		{
			removeJ(ROW,COLUMN);
		}
		else if(brick==5)
		{
			removeL(ROW,COLUMN);
		}
		else if(brick==6)
		{
			removeT(ROW,COLUMN);
		}
		else if(brick==7)
		{
			removeLineH(ROW,COLUMN);
		}
		else if(brick==8)
		{
			removeTL(ROW,COLUMN);
		}
		else if(brick==9)
		{
			removeTR(ROW,COLUMN);
		}
		else if(brick==10)
		{
			removeTD(ROW,COLUMN);
		}
		else if(brick==11)
		{
			removeJ1(ROW,COLUMN);
		}
		else if(brick==12)
		{
			removeJ2(ROW,COLUMN);
		}
		else if(brick==13)
		{
			removeJ3(ROW,COLUMN);
		}
		else if(brick==14)
		{
			removeL1(ROW,COLUMN);
		}
		else if(brick==15)
		{
			removeL2(ROW,COLUMN);
		}
		else if(brick==16)
		{
			removeL3(ROW,COLUMN);
		}
		else if(brick==17)
		{
			removeLineV(ROW,COLUMN);
		}
		else if(brick==21)
		{
			removeZ2(ROW,COLUMN);
		}
		else if(brick==22)
		{
			removeZInverted2(ROW,COLUMN);
		}
	}
	
	public void switchShape(int row,int column,GraphicsContext gc, GraphicsContext gc2)
	{
	removeBrick(brick,ROW-1,COLUMN);
	clearNext();
	brick=nextBrick();
	count++;
	score=score-level*m;
	while(checkBrick(brick,row-1,column))
	{
		ROW=row;
		COLUMN=column;
		brick=nextBrick();
		
	}
	addBrick(brick);
	nextBrick=nextBrick();
	addNext();
	drawBrick(gc,size);
	drawNext(gc2,size);
	}
	
	
	
}
