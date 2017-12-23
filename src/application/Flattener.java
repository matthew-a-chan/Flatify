package application;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * @author Matthew C.
 *	This is the flattener class. It flattens images. (hopefully)
 */
public class Flattener extends Application {

	int resolution=45;
	int closestNode=0;


	@Override
	/**
	 * This method starts the UI for the Flattener
	 */
	public void start(Stage primaryStage) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Pick Image");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("JPEG files", "*.jpeg"),new ExtensionFilter("PNG files","*.png"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")+"/Desktop"));
		File file = fileChooser.showOpenDialog(primaryStage);
		if (file == null) {
			System.exit(0);
		}


		ImageView tempImage=new ImageView(file.toURI().toString());
		Image imageUnblanked=tempImage.getImage();

		tempImage.setScaleY(500/tempImage.getImage().getHeight());
		tempImage.setScaleX(500/tempImage.getImage().getHeight());


		//CREATING THE STARTER WINDOW
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root,1000,500);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);

		tempImage.setLayoutX((500-tempImage.getImage().getWidth())/2);
		tempImage.setLayoutY((500-tempImage.getImage().getHeight())/2);
		root.getChildren().add(tempImage);

		Polygon poly=new Polygon();
		root.getChildren().add(poly);
		poly.setFill(new Color(.5,.5,.5,.5));

		poly.getPoints().addAll(new Double[]{
				150.0,100.0,
				350.0,100.0,
				350.0,400.0,
				150.0,400.0});
		Point2D.Double tL=new Point2D.Double(poly.getPoints().get(0),poly.getPoints().get(1));
		Point2D.Double tR=new Point2D.Double(poly.getPoints().get(2),poly.getPoints().get(3));
		Point2D.Double bR=new Point2D.Double(poly.getPoints().get(4),poly.getPoints().get(5));
		Point2D.Double bL=new Point2D.Double(poly.getPoints().get(6),poly.getPoints().get(7));

		poly.setOnMousePressed(Event ->{
			tL.setLocation(poly.getPoints().get(0),poly.getPoints().get(1));
			tR.setLocation(poly.getPoints().get(2),poly.getPoints().get(3));
			bR.setLocation(poly.getPoints().get(4),poly.getPoints().get(5));
			bL.setLocation(poly.getPoints().get(6),poly.getPoints().get(7));
			Point2D.Double down=new Point2D.Double(Event.getX(),Event.getY());
			if(down.distance(tL)<Math.min(Math.min(down.distance(tR),down.distance(bR)),down.distance(bL)))
				closestNode=0;
			else if(down.distance(tR)<Math.min(down.distance(bR), down.distance(bL)))
				closestNode=1;
			else if(down.distance(bR)<down.distance(bL))
				closestNode=2;
			else
				closestNode=3;
		});
		poly.setOnMouseDragged(Event ->{
			poly.getPoints().set(closestNode*2,Event.getX());
			poly.getPoints().set(closestNode*2+1,Event.getY());
		});




		BorderPane instr=new BorderPane();
		instr.setLayoutX(500);
		instr.setLayoutY(0);
		root.getChildren().add(instr);



		Button button=new Button();
		button.setOnAction(event ->{
			java.awt.Polygon blocked=new java.awt.Polygon();
			blocked.addPoint((int)((poly.getPoints().get(0)-(500-imageUnblanked.getWidth()*500/imageUnblanked.getHeight())/2)*imageUnblanked.getHeight()/500),(int)(poly.getPoints().get(1)*imageUnblanked.getHeight()/500));
			blocked.addPoint((int)((poly.getPoints().get(2)-(500-imageUnblanked.getWidth()*500/imageUnblanked.getHeight())/2)*imageUnblanked.getHeight()/500),(int)(poly.getPoints().get(3)*imageUnblanked.getHeight()/500));
			blocked.addPoint((int)((poly.getPoints().get(4)-(500-imageUnblanked.getWidth()*500/imageUnblanked.getHeight())/2)*imageUnblanked.getHeight()/500),(int)(poly.getPoints().get(5)*imageUnblanked.getHeight()/500));
			blocked.addPoint((int)((poly.getPoints().get(6)-(500-imageUnblanked.getWidth()*500/imageUnblanked.getHeight())/2)*imageUnblanked.getHeight()/500),(int)(poly.getPoints().get(7)*imageUnblanked.getHeight()/500));
			WritableImage image=new WritableImage((int)imageUnblanked.getWidth(),(int)imageUnblanked.getHeight());
			PixelWriter writer=image.getPixelWriter();
			PixelReader reader=imageUnblanked.getPixelReader();
			for(int y=0;y<imageUnblanked.getHeight();y++){
				for(int x=0;x<imageUnblanked.getWidth();x++){
					if(blocked.contains(x,y)){
						writer.setColor(x, y, reader.getColor(blocked.xpoints[0],blocked.ypoints[0]));
					}
					else{
						writer.setColor(x, y, reader.getColor(x, y));
					}
				}
			}
			flatten(image, imageUnblanked);
		});
		button.setText("Flatten Image");
		button.setPrefSize(200,200);
		button.setMinHeight(200);
		button.setMinWidth(200);
		button.setLayoutX(266);
		button.setLayoutY(250);
		button.setVisible(true);
		button.autosize();
		instr.getChildren().add(button);


		MenuItem menuItem1 = new MenuItem("Low Quality");
		MenuItem menuItem2 = new MenuItem("Medium Quality");
		MenuItem menuItem3 = new MenuItem("High Quality");
		MenuItem menuItem4 = new MenuItem("Ultra-HD");

		MenuButton ResolutionButton=new MenuButton("Options", null, menuItem1, menuItem2, menuItem3, menuItem4);

		menuItem1.setOnAction(event -> {
			resolution=32;
			ResolutionButton.setText("Low Quality");
		});
		menuItem2.setOnAction(event -> {
			resolution=16;
			ResolutionButton.setText("Medium Quality");
		});
		menuItem3.setOnAction(event -> {
			resolution=8;
			ResolutionButton.setText("High Quality");
		});
		menuItem4.setOnAction(event -> {
			resolution=2;
			ResolutionButton.setText("Ultra-HD");
		});


		ResolutionButton.setText("Select Quality");
		ResolutionButton.setPrefSize(200,50);
		ResolutionButton.setMinHeight(50);
		ResolutionButton.setMinWidth(200);
		ResolutionButton.setLayoutX(33);
		ResolutionButton.setLayoutY(250);
		ResolutionButton.setVisible(true);
		ResolutionButton.autosize();
		instr.getChildren().add(ResolutionButton);



		Text text = new Text();
		text.setLayoutX(50);
		text.setLayoutY(50);
		text.setFont(new Font(15));
		text.setWrappingWidth(430);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setText("Instructions:\nSelect the image that you would like to flatten"
				+ " then drag the gray polygon over as much text as you can without obscuring the edges of the page."
				+ " Please try not to lose the box. You'll have to restart the program if you do."
				+ " You can select the quality of the image too."
				+ " Higher qualities and larger images take longer to process. (Duh.)"
				+ " When you're ready, press the flatten button.");
		text.setVisible(true);
		text.minHeight(100);
		text.minWidth(100);
		instr.getChildren().add(text);

		primaryStage.show();

	}


	/**
	 * @author Matthew C.
	 *
	 * @param args The aruments passed into the program by the OS. They don't do anything.
	 * Launches the FX Application (Flattener)
	 */
	public static void main(String[] args) {
		launch(args);
	}












	private void flatten(Image image,Image unBlanked){

		//This creates a 2d array of doubles representing the brigtness level of the grayscale of the pixel at (x,y)
		PixelReader reader=image.getPixelReader();
		double[][]brights=new double[(int) image.getWidth()][(int) image.getHeight()];
		for(int y=0;y<image.getHeight();y++){
			for(int x=0;x<image.getWidth();x++){
				Color color=reader.getColor(x, y);
				brights[x][y]=color.grayscale().getBrightness();
			}
		}
		boolean[][]diffs=new boolean[brights.length][brights[0].length*2-1];

		double sensitivity=.2;

		//This section creats a 2d array of Booleans of size x = brights.length, y = brights.length*2-1
		//If a coordinate (x,y) in this array is true, it means that there is a steep contrast difference in either the x or y direction at that point.
		//The even rows are used to store horizontal differences, odd rows store vertical differences


		//VERTICAL DIFFS - on odd Y's
		for(int y=1;y<brights[0].length;y++){
			for(int x=0;x<brights.length-1;x++){
				if(y+2<brights[0].length&&y-3>=0&&(Math.abs(brights[x][y-3]-brights[x][y+2])>sensitivity))
					diffs[x][2*y-1]=true;
				else if(y+1<brights[0].length&&y-2>=0&&(Math.abs(brights[x][y-2]-brights[x][y+1])>sensitivity))
					diffs[x][2*y-1]=true;
				else if((Math.abs(brights[x][y-1]-brights[x][y])>sensitivity))
					diffs[x][2*y-1]=true;
			}
		}

		//HORIZONTAL DIFFS - on even Y's
		for(int y=0;y<brights[0].length;y++){
			for(int x=1;x<brights.length;x++){
				if(x-3>=0&&x+2<brights.length&&(Math.abs(brights[x-3][y]-brights[x+2][y])>sensitivity))
					diffs[x-1][2*y]=true;
				if(x-2>=0&&x+1<brights.length&&(Math.abs(brights[x-2][y]-brights[x+1][y])>sensitivity))
					diffs[x-1][2*y]=true;
				else if((Math.abs(brights[x-1][y]-brights[x][y])>sensitivity))
					diffs[x-1][2*y]=true;

			}
		}

		//This creates new groups (see below for the class group)
		//It iterates through the whole array of diffs and adds each true to an adjacent group (if there's none nearby, it creates a new one)

		brights=null;
		ArrayList<group> clumps=new ArrayList<group>();
		for(int y=0;y<diffs[0].length;y++){
			for(int x=0;x<diffs.length;x++){
				if(diffs[x][y]){
					boolean grouped=false;
					found:for(int i=0;i<clumps.size();i++){
						group temp=clumps.get(i);
						if(temp!=null&&temp.overlaps(new Point(x,y))){
							group newTemp=new group(temp,new group(x,y));
							clumps.remove(temp);
							clumps.add(newTemp);
							grouped=true;
							break found;
						}
					}
					if(!grouped){
						clumps.add(new group(x,y));
					}
				}
			}
		}

		//This loops through the groups, starting with the smallest and going to the largest.
		//It checks if this group is adjacent to another group, if they are, it combines the two groups and adds it back into the array.
		//If it doesn't intersect anything, it removes it from the array of groups and sets all of the coords within that group to false.
		//This removes all of the small groups, so that the only remaining group is the largest group, and all diffs are false except those within that group.

		while(clumps.size()>1){
			Collections.sort(clumps);
			group temp=clumps.remove(0);
			boolean grouped=false;
			for(int i=0;i<clumps.size()&&true;i++){
				if(temp.overlaps(clumps.get(i))){
					clumps.add(new group(temp,clumps.remove(i)));
					grouped=true;
					break;
				}
			}
			if(!grouped)
				if(!grouped){
					for(int i=0;i<temp.size;i++){
						diffs[temp.points.get(i).x][temp.points.get(i).y]=false;
					}
				}
		}


		//Each one of these determines the wall by moving in from the left(or whatever side it is) towards the middle and stops when it hits a true in the diffs[][] array
		//This finds the coords of the walls

		ArrayList<Point>leftWall=new ArrayList<Point>();
		for(int y=0;y<diffs[0].length;y+=2){
			for(int x=0;x<diffs.length;x++){

				try{
					if(diffs[x-1][y-1]||diffs[x-1][y]||diffs[x-1][y+1]||diffs[x][y-1]||diffs[x][y]||diffs[x][y+1]||diffs[x+1][y-1]||diffs[x+1][y]||diffs[x+1][y+1]){
						leftWall.add(new Point(x,y/2));
						break;
					}
				}
				catch(IndexOutOfBoundsException e){
					if(clumps.get(0).overlaps(new Point(x,y))){
						leftWall.add(new Point(x,y/2));
						break;
					}
				}
			}
		}


		ArrayList<Point>rightWall=new ArrayList<Point>();
		for(int y=0;y<diffs[0].length;y+=2){
			for(int x=diffs.length-1;x>=0;x--){

				try{
					if(diffs[x-1][y-1]||diffs[x-1][y]||diffs[x-1][y+1]||diffs[x][y-1]||diffs[x][y]||diffs[x][y+1]||diffs[x+1][y-1]||diffs[x+1][y]||diffs[x+1][y+1]){
						rightWall.add(new Point(x,y/2));
						break;
					}
				}
				catch(IndexOutOfBoundsException e){
					if(clumps.get(0).overlaps(new Point(x,y))){
						rightWall.add(new Point(x,y/2));
						break;
					}
				}
			}
		}



		ArrayList<Point>topWall=new ArrayList<Point>();
		for(int x=0;x<diffs.length;x++){
			for(int y=0;y<diffs[0].length;y++){
				try{
					if(diffs[x-1][y-1]||diffs[x-1][y]||diffs[x-1][y+1]||diffs[x][y-1]||diffs[x][y]||diffs[x][y+1]||diffs[x+1][y-1]||diffs[x+1][y]||diffs[x+1][y+1]){
						topWall.add(new Point(x,y/2));
						break;
					}
				}
				catch(IndexOutOfBoundsException e){
					if(clumps.get(0).overlaps(new Point(x,y))){
						topWall.add(new Point(x,y/2));
						break;
					}
				}
			}
		}



		ArrayList<Point>botWall=new ArrayList<Point>();
		for(int x=0;x<diffs.length;x++){
			for(int y=diffs[0].length-1;y>0;y--){
				try{
					if(diffs[x-1][y-1]||diffs[x-1][y]||diffs[x-1][y+1]||diffs[x][y-1]||diffs[x][y]||diffs[x][y+1]||diffs[x+1][y-1]||diffs[x+1][y]||diffs[x+1][y+1]){
						botWall.add(new Point(x,y/2));
						break;
					}
				}
				catch(IndexOutOfBoundsException e){
					if(clumps.get(0).overlaps(new Point(x,y))){
						botWall.add(new Point(x,y/2));
						break;
					}
				}
			}
		}

		//This trims the walls, so that they are more accurate and the noise on the ends gets cut off

		ArrayList<ArrayList<Point>> walls=new ArrayList<ArrayList<Point>>();
		walls.add(leftWall);
		walls.add(rightWall);
		walls.add(topWall);
		walls.add(botWall);
		int dist=1;
		for(ArrayList<Point>wall:walls){
			int mid=wall.size()/2;
			for(int i=mid;i<wall.size()-dist;i++){
				Point prev=wall.get(i-dist);
				Point curr=wall.get(i);
				Point ideal=new Point(2*curr.x-prev.x,2*curr.y-prev.y);
				Point next=wall.get(i+dist);
				if(curr.distance(next)>4){
					wall.remove(next);
					i--;
				}
			}
			for(int i=mid;i>dist-1;i--){
				Point prev=wall.get(i+dist);
				Point curr=wall.get(i);
				Point ideal=new Point(2*curr.x-prev.x,2*curr.y-prev.y);
				Point next=wall.get(i-dist);
				if(curr.distance(next)>4){
					wall.remove(next);
					i++;
				}
			}
		}

		diffs=null;
		clumps=null;

		//This initializes the dot-grid along the entire image by measuring fractions of the wall lengths.

		WritableImage tempImage=new WritableImage(unBlanked.getPixelReader(),(int)unBlanked.getWidth(),(int)unBlanked.getHeight());

		PixelWriter writer=tempImage.getPixelWriter();

		double res=(int)(image.getHeight()/resolution);
		Point[][]corners=new Point[(int) res+1][(int) res+1];
		/*for(int x=0;x<50;x++){
			for(int y=0;y<50;y++){
				for(int i=-2;i<=2;i++){
					for(int k=-2;k<=2;k++){
						writer.setColor((int)(leftWall.get((int) ((leftWall.size()-1)*y/50)).x*(50-x)/50+rightWall.get((int) ((rightWall.size()-1)*y/50)).x*x/50)+i,
								(int)(topWall.get((int) ((topWall.size()-1)*x/50)).y*(50-y)/50+botWall.get((int) ((botWall.size()-1)*x/50)).y*y/50)+k, Color.RED);
					}
				}
			}
		}*/
		for(int x=0;x<=res;x++){
			for(int y=0;y<=res;y++){
				if(x==0||x==res||y==0||y==res){
					if(y==0||y==res){
						corners[x][y]=new Point((int)(topWall.get((int) ((topWall.size()-1)*(res-x)/res)).x*(res-y)/res  +  botWall.get((int) ((botWall.size()-1)*x/res)).x*y/res),
								(int)(topWall.get((int) ((topWall.size()-1)*(res-x)/res)).y*(res-y)/res  +  botWall.get((int) ((botWall.size()-1)*x/res)).y*y/res));
					}
					if(x==0||x==res){
						corners[x][y]=new Point((int)(leftWall.get((int) ((leftWall.size()-1)*(res-y)/res)).x*(res-x)/res  +  rightWall.get((int) ((rightWall.size()-1)*y/res)).x*x/res),
								(int)(leftWall.get((int) ((leftWall.size()-1)*(res-y)/res)).y*(res-x)/res  +  rightWall.get((int) ((rightWall.size()-1)*y/res)).y*x/res));
					}
				}
				else{
					corners[x][y]=new Point((int)(leftWall.get((int) ((leftWall.size()-1)*y/res)).x*(res-x)/res+rightWall.get((int) ((rightWall.size()-1)*y/res)).x*x/res),
							(int)(topWall.get((int) ((topWall.size()-1)*x/res)).y*(res-y)/res+botWall.get((int) ((botWall.size()-1)*x/res)).y*y/res));

				}
			}
		}
		for(ArrayList<Point>wall:walls){
			Color asdf=new Color(Math.random(),Math.random(),Math.random(), 1);
			for(Point a:wall){
				writer.setColor(a.x, a.y, asdf);
			}
		}

		WritableImage finalImage=new WritableImage((int)(topWall.size()+botWall.size())/2,(int)(leftWall.size()+rightWall.size())/2);//(int)image.getWidth(), (int)image.getHeight());
		PixelWriter finalWriter=finalImage.getPixelWriter();

		//Ignore this here \/ \/ \/
		//average slopes of either side (weighted by dist to each opposite line (the two verticals))
		//make the line going through point (x,y) and get intercept on the two horizontal lines
		//compare distnace to get the y coord of the point in the new graph
		//repeat for x coords

		//This loops through each of the boxes created by the box grids and rectangularizes it, then copies it to a new image.
		//I'd explain it if I had more time... but it's 3:27 right now :/
		//At the end of each box, it copies the new image onto the final image in the location it should be.

		PixelReader pixelSpot=unBlanked.getPixelReader();
		java.awt.Polygon[][] boxes=new java.awt.Polygon[(int) res][(int) res];
		for(int x=0;x<res;x++){
			for(int y=0;y<res;y++){
				boxes[x][y]=new java.awt.Polygon();
				boxes[x][y].addPoint(corners[x][y].x,corners[x][y].y);
				boxes[x][y].addPoint(corners[x+1][y].x,corners[x+1][y].y);
				boxes[x][y].addPoint(corners[x+1][y+1].x,corners[x+1][y+1].y);
				boxes[x][y].addPoint(corners[x][y+1].x,corners[x][y+1].y);
				WritableImage newImage=new WritableImage((int)(image.getWidth()/res), (int)(image.getHeight()/res));
				PixelWriter tempWriter=newImage.getPixelWriter();
				for(int x1=Math.min(Math.min(boxes[x][y].xpoints[0],boxes[x][y].xpoints[1]),Math.min(boxes[x][y].xpoints[2],boxes[x][y].xpoints[3]));x1<=Math.max(Math.max(boxes[x][y].xpoints[0],boxes[x][y].xpoints[1]),Math.max(boxes[x][y].xpoints[2],boxes[x][y].xpoints[3]));x1++){
					for(int y1=Math.min(Math.min(boxes[x][y].ypoints[0],boxes[x][y].ypoints[1]),Math.min(boxes[x][y].ypoints[2],boxes[x][y].ypoints[3]));y1<=Math.max(Math.max(boxes[x][y].ypoints[0],boxes[x][y].ypoints[1]),Math.max(boxes[x][y].ypoints[2],boxes[x][y].ypoints[3]));y1++){
						Point p=new Point(x1,y1);
						if(boxes[x][y].contains(p)){
							Line2D.Double top=new Line2D.Double(corners[x][y],corners[x+1][y]);
							Line2D.Double bot=new Line2D.Double(corners[x][y+1],corners[x+1][y+1]);
							Line2D.Double left=new Line2D.Double(corners[x][y],corners[x][y+1]);
							Line2D.Double right=new Line2D.Double(corners[x+1][y],corners[x+1][y+1]);
							double angle1 = Math.atan2(top.getY1() - top.getY2(),
									top.getX1() - top.getX2());//+2*Math.PI;
							double angle2 = Math.atan2(bot.getY1() - bot.getY2(),
									bot.getX1() - bot.getX2());//+2*Math.PI;
							Line2D.Double horizontal=new Line2D.Double(
									p.x-image.getWidth()/res,
									p.y-Math.tan((angle1*
											(top.ptLineDist(p)/(top.ptLineDist(p)+bot.ptLineDist(p)))
											+angle2*
											(bot.ptLineDist(p)/(top.ptLineDist(p)+bot.ptLineDist(p)))
											))
									*2*image.getWidth()/res,
									p.x+image.getWidth()/res, 
									p.y+Math.tan((angle1*
											(top.ptLineDist(p)/(top.ptLineDist(p)+bot.ptLineDist(p)))
											+angle2*
											(bot.ptLineDist(p)/(top.ptLineDist(p)+bot.ptLineDist(p)))
											))
									*2*image.getWidth()/res);


							double lr=p.distance(getLineIntersect(horizontal,left))/(p.distance(
							getLineIntersect(horizontal,left))+p.distance(getLineIntersect(horizontal,right)));


							angle1 = Math.atan2(left.getY1() - left.getY2(),
									left.getX1() - left.getX2());//+2*Math.PI;
							angle2 = Math.atan2(right.getY1() - right.getY2(),
									right.getX1() - right.getX2());//+2*Math.PI;
							Line2D.Double vertical=new Line2D.Double(
									p.x-2*image.getHeight()/res/
									Math.tan((angle1*
											(left.ptLineDist(p)/(left.ptLineDist(p)+right.ptLineDist(p)))
											+angle2*
											(right.ptLineDist(p)/(left.ptLineDist(p)+right.ptLineDist(p)))
											)),
									p.y-image.getHeight()/res,
									p.x+2*image.getHeight()/res/
									Math.tan((angle1*
											(left.ptLineDist(p)/(left.ptLineDist(p)+right.ptLineDist(p)))
											+angle2*
											(right.ptLineDist(p)/(left.ptLineDist(p)+right.ptLineDist(p)))
											)),
									p.y+image.getHeight()/res);



							double tb=p.distance(getLineIntersect(vertical,top))/(p.distance(
							getLineIntersect(vertical,top))+p.distance(getLineIntersect(vertical,bot)));


							tempWriter.setColor(
									(int)((newImage.getWidth()-1)*lr), 
									(int)((newImage.getHeight()-1)*tb),
									pixelSpot.getColor(x1, y1));
						}


					}
				}
				finalWriter.setPixels((int)((finalImage.getWidth()-1)/res*x), (int)((finalImage.getHeight()-1)/res*y), (int)(Math.min(newImage.getWidth(),finalImage.getWidth()-newImage.getWidth())), (int)(Math.min(newImage.getHeight(),finalImage.getHeight()-newImage.getHeight())), newImage.getPixelReader(), 0, 0);
			}
		}

		//This goes through the image and seals each of the blank images with averages of the values around it.



		PixelReader finalReader=finalImage.getPixelReader();

		for(int x=0;x<finalImage.getWidth();x++){
			for(int y=0;y<finalImage.getHeight();y++){
				if(finalReader.getColor(x, y).getOpacity()==0){
					double aR=0;
					double aG=0;
					double aB=0;
					int i=0;
					for(int x1=-1;x1<=1;x1++){
						for(int y1=-1;y1<=1;y1++){
							if(x+x1>=0&&x+x1<finalImage.getWidth()&&y+y1>=0&&y+y1<finalImage.getHeight()&&finalReader.getColor(x+x1, y+y1).getOpacity()!=0){
								aR+=finalReader.getColor(x+x1, y+y1).getRed();
								aG+=finalReader.getColor(x+x1, y+y1).getGreen();
								aB+=finalReader.getColor(x+x1, y+y1).getBlue();
								i++;
							}
						}
					}
					finalWriter.setColor(x, y, new Color(aR/i,aG/i,aB/i,1));
				}
			}
		}


		//Creates the files and outputs them to the locations.

		try {
			File file=new File("outLines(DEBUG).png");
			RenderedImage imagen=SwingFXUtils.fromFXImage(tempImage, null);
			ImageIO.write(imagen, "png", file);
			File finalFile=new File(System.getProperty("user.home")+"/Desktop/final.png");
			RenderedImage finalOut=SwingFXUtils.fromFXImage(finalImage, null);
			ImageIO.write(finalOut, "png", finalFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}












	private Point2D.Double getLineIntersect(Line2D.Double one,Line2D.Double two){
		double m1 = 0.0; // slope of first line
		double b1 = 0.0; // y-intercept of first line
		double m2 = 0.0; // slope of second line
		double b2 = 0.0; // y-intercept of second line

		double x = 0.0; // (x, y) point of intersection.
		double y = 0.0;


		// get slopes and y-intercepts
		m1 = (one.y2-one.y1)/(one.x2-one.x1);
		b1 = one.y1-one.x1*m1;
		m2 = (two.y2-two.y1)/(two.x2-two.x1);
		b2 = two.y2-two.x2*m2;


		if(one.x1!=one.x2&&two.x1!=two.x2){
			x = (b2 - b1) / (m1 - m2); // solve for x-coordinate of intersection
			y = m1 * x + b1; // solve
		}
		else{
			if(one.x1==one.x2){
				x=one.x1;
				y = m2 * x + b2;
			}
			else{
				x=two.x1;
				y = m1 * x + b1; // solve
			}
		}
		return new Point2D.Double(x,y);
	}


	private class group implements Comparable<group>{
		int size=0;
		ArrayList<Point>points=new ArrayList<Point>();
		group(int x,int y){
			size=1;
			Point newPoint=new Point(x,y);
			points.add(newPoint);
		}

		group(group a, group b){
			this.size=a.size+b.size;
			for(int i=0;i<a.size;i++){
				this.points.add(a.points.get(i));
			}
			for(int i=0;i<b.size;i++){
				this.points.add(b.points.get(i));
			}
		}

		//This checks if any part of a group is within 2.25 of a point.
		public boolean overlaps(Point a){
			for(int i=0;i<size;i++){
				if(this.points.get(i).distance(a)<2.25){
					return true;
				}
			}
			return false;
		}

		//This checks if any part of any group is within 2.25 of any part of another group.
		public boolean overlaps(group a){
			for(int i=0;i<a.size;i++){
				if(overlaps(a.points.get(i))){
					return true;
				}
			}
			return false;
		}

		@Override
		public int compareTo(group o) {
			if(this.size<o.size)
				return -1;
			if(this.size>o.size)
				return 1;
			return 0;
		}
	}
}
