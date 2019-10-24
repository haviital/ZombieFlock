import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.Sprite;
import femto.input.Button;
import femto.sound.Mixer;
import femto.sound.Procedural;
import femto.Image;
import femto.palette.Pico8;

import Common;
import Event;

public class Hero3D extends Hero
{
    float vec3Dx;
    float vec3Dy;
    float vec3Dz;
    float size;
    float speedZ = -0.3;
    
    
    void update()
    {
        vec3Dz += speedZ;
        if(vec3Dz<=5)
            vec3Dz = 150;

        float factor = 1.0 / vec3Dz;
        float newY = 4*176 * factor;
        y = newY + 20;
        
        size = factor*200;
    }

    void drawMe()
    {
        if(vec3Dz<100)
            rotozoom(Main.screen, 0, size);
    }

}

public class Enemy3D extends Enemy
{
    float vec3Dx;
    float vec3Dy;
    float vec3Dz;
    float size;
    float speedZ = -0.3;
    
    void update()
    {
        vec3Dz += speedZ;
        if(vec3Dz<=5)
            vec3Dz = 150;

        float factor = 1.0 / vec3Dz;

        float newY = 4*176 * factor;
        y = newY + 20;
        
        float newX = 10 * vec3Dx * factor;
        x = 110 + newX;
        
        size = factor*200;
    }
    
    void drawMe()
    {
        if(vec3Dz<100)
            rotozoom(Main.screen, 0, size);
    }
}

public class MainFinished extends State 
{
    
    // Member data
    long programStartTimeMs; // the start time of the program
    Hero3D hero;    
    Enemy3D enemyArr[];    
    long changeColorAt;
    int colorIndex;
    long totalPoints;

    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){

        Main.screen.loadPalette(Pico8.palette());

        Main.screen.cameraX = 0;
        Main.screen.cameraY = 0;
        
        // minus 1 so that the Common.currentFrameStartTimeMs do not start from 0.
        programStartTimeMs = System.currentTimeMillis() - 1; 
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;
        
        changeColorAt = Common.currentFrameStartTimeMs + 200;
        
        hero = new Hero3D();
        hero.run();
        hero.x = 110;
        hero.y = 88;
        hero.vec3Dx = 0;
        hero.vec3Dz = 150;

        enemyArr = new Enemy3D[3];
        for(int i=0; i < 3; i++)
        {
            enemyArr[i] = new Enemy3D();
            enemyArr[i].run();
        }
        enemyArr[0].x = 110;
        enemyArr[0].y = 88;
        enemyArr[0].vec3Dx = 0;
        enemyArr[0].vec3Dz = 140;
        enemyArr[1].x = 110-40;
        enemyArr[1].y = 88;
        enemyArr[1].vec3Dx = -40;
        enemyArr[1].vec3Dz = 140;
        enemyArr[2].x = 110+40;
        enemyArr[2].y = 88;
        enemyArr[2].vec3Dx = 40;
        enemyArr[2].vec3Dz = 140;
        
        // Calc total points
        totalPoints = 0;
        for(int i=0; i<Common.LEVEL_MAP_COUNT; i+=1)
        {
            totalPoints += 10 * Common.levelPointsArray[i];
        }
        
    }
    
    // 
    void shutdown()
    {
        //
        Common.freeResources();
    }
    
    // update is called by femto.Game every frame
    void update(){
        
        // Read the current time. Substract the program start time to make the value smaller, so it fits to 32 bits. 
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;

        // *** UPDATE
        
        // Pressed A in the back story window?
        boolean justPressedA = Button.A.justPressed();
        if( justPressedA )
        {
        }
        
        
        hero.update();
        
        for(int i=0; i < 3; i++)
        {
            enemyArr[i].update();
        }
        
        // *** DRAW
        
        // if(Common.currentFrameStartTimeMs>changeColorAt)
        // {
        //     changeColorAt = Common.currentFrameStartTimeMs + 500;
        //     if(++colorIndex > 15)
        //         colorIndex = 0;
        // }
        Main.screen.clear(12);
        
        hero.drawMe();
        
        for(int i=0; i < 3; i++)
        {
            enemyArr[i].drawMe();
        }
        
        Main.drawTextCellCentered( 0, 5, 220, "CONGRATULATIONS ! You have now enough");
        Main.drawTextCellCentered( 0, 15, 220, "power to exile the undeads for good!");
        
        Main.drawTextCellCentered( 0, 176-10, 220, "Best points: " + totalPoints);
        
       // Update the screen with everything that was drawn
        Main.screen.flush();
    }
 
 
}


