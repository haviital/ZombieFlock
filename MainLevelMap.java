import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.Sprite;
import femto.input.Button;
import ZxSpectrum;
import femto.font.TIC80;
import femto.sound.Mixer;
import femto.sound.Procedural;
//import femto.math;
import femto.palette.Pico8;

import EnemyEntity;
import HorizBarEntity;
import VertBarEntity;
import TimeMeterEntity;
import Common;
import Event;
import maps.map;

public class MainLevelMap extends State 
{
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init()
    {
        Main.screen.cameraX = 0;
        Main.screen.cameraY = 0;
        
        levelMapItemImage = new LevelMapItem();
        lockImage = new Lock();
        endsImage = new PointsRectEnds();
        bodyImage = new PointsRectBody();
        coffee = new Coffee();
        coffee.run(); // "run" is one of the animations in the spritesheet
    }
    
    // 
    void shutdown()
    {
        Common.freeResources();
    }
    
    // update is called by "femto.Game" every frame
    void update(){
        
        // 
        if( Button.Left.justPressed() )
        {
            gridPosX -= 1;
            if(gridPosX< 0)
                gridPosX = 2;
        }
        else if( Button.Right.justPressed() )
        {
            gridPosX += 1;
            if(gridPosX>2)
                gridPosX = 0;
        }
        if( Button.Up.justPressed() )
        {
            gridPosY -= 1;
            if(gridPosY< 0)
                gridPosY = 1;
        }
        else if( Button.Down.justPressed() )
        {
            gridPosY += 1;
            if(gridPosY>1)
                gridPosY = 0;
        }
        
        
        if( Button.A.justPressed() )
        {
            // Go on to the night level.
            int dayNum = gridPosX + (gridPosY*3);
            Common.currentDay = dayNum;
            Game.changeState( new MainDay() );
        }

        if( Button.B.justPressed() ) 
        {
            //save.score = Math.random(0, 1000); // update the score
        }

        
        // *** DRAW
        
        // Clear
        Main.screen.clear(0);
        
        // Draw points
        
        // Draw left end
        long currX = 4;
        long currY = 3;
        endsImage.draw( Main.screen, currX, currY );
        currX += 22;
        
        // Draw body
        for(int i=0; i<11; i+=1)
        {
            bodyImage.draw( Main.screen, currX, currY );
            currX += 16;
        }
            
        // Draw right end
        currX = 220-4-32;
        endsImage.draw( Main.screen, currX, currY );
        currX += 32 - 10 - 16;
        bodyImage.draw( Main.screen, currX, currY );
        currX += - 16;
        bodyImage.draw( Main.screen, currX, currY );

        // Draw total points.
        currY = 12;
        String text = "TOTAL POINTS: " + 440;
        int pixelwidth = Main.screen.textWidth(text);
        currX = 110-(pixelwidth/2);
        Main.screen.setTextPosition( currX+1, currY+1 );
        Main.screen.textColor = 1;
        Main.screen.print( text );
        Main.screen.setTextPosition( currX, currY );
        Main.screen.textColor = 3;
        Main.screen.print(  text);
            
        // Draw the level map
        for(int i=0; i<Common.LEVEL_MAP_COUNT; i+=1)
        {
            int margin = 3;
            int row = i / 3;
            int col = i % 3;
            int width = (220/3);
            int height = (176/2) - 17;
            int topLeftX = col *  width + 1;
            int topLeftY = row * height + 14 + 17;

            // Draw level item bg.
            long color = 10;
            if(col == gridPosX && row == gridPosY )
                color = 5;
            Main.screen.fillRect( topLeftX + margin+3,  topLeftY + margin+3, 
                    levelMapItemImage.width()-6, levelMapItemImage.height()-6, color );
            
            // Draw level item
            levelMapItemImage.draw( Main.screen,  topLeftX + margin, topLeftY + margin );
            
            
            if( Common.levelPointsArray[i]>0)
            {
                // Draw day n.
                Main.screen.setTextPosition( topLeftX + margin + 18 + 1, topLeftY + margin + 23 + 1 );
                Main.screen.textColor = 1;
                Main.screen.print("DAY " + (i+1) );
                Main.screen.setTextPosition( topLeftX + margin + 18, topLeftY + margin + 23 );
                Main.screen.textColor = 3;
                Main.screen.print("DAY " + (i+1) );
                
                // Draw coffee cup.
                coffee.draw(Main.screen, topLeftX + margin + 18, topLeftY + margin + 32);
    
                // Draw coffee factor.
                Main.screen.setTextPosition( topLeftX + margin + 32 + 1, topLeftY + margin + 36 + 1 );
                Main.screen.textColor = 1;
                Main.screen.print("x" +  Common.levelPointsArray[i] );
                Main.screen.setTextPosition( topLeftX + margin + 32, topLeftY + margin + 36 );
                Main.screen.textColor = 3;
                Main.screen.print("x" +  Common.levelPointsArray[i] );
            }
            else
            {
                // Draw lock.
                lockImage.draw(Main.screen, topLeftX + margin + 23, topLeftY + margin + 20);
            }
        }

        // Update the screen with everything that was drawn
        Main.screen.flush();
    }
    
    
    LevelMapItem levelMapItemImage;
    Lock lockImage;
    PointsRectEnds endsImage;
    PointsRectBody bodyImage;
    Coffee coffee;
    long gridPosX;
    long gridPosY;
}


