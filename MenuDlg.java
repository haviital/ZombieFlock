import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class MenuDlg 
{
   float winX;
   float winY;
   float winW;
   float winH;
   String texts[];
   int focusIndex;
   boolean pressedA;
   
    MenuDlg(float winXPar, float winYPar, float winWPar, float winHPar, String textsPar[]) 
    { 
        winX = winXPar;
        winY = winYPar;
        winW = winWPar;
        winH = winHPar;
        texts = textsPar;
    }
    
    void update()
    {
        pressedA = false;
        if( Button.A.justPressed() )
        {
            pressedA = true;
        }

        if( Button.Down.justPressed() )
        {
            focusIndex += 1;
            if( focusIndex >= texts.length)
                focusIndex = texts.length-1;
        }
        else if( Button.Up.justPressed() )
        {
            focusIndex -= 1;
            if( focusIndex < 0 )
                focusIndex = 0;
        }
    }

    void draw()
    {
        drawMenu();
    }
    
    void drawMenu()    
    {
        int marginY = 11;
        int focusMarginY = 25;
        
        Main.DrawPanel((int)winX, (int)winY, (int)winW, (int)winH);
        
        for( int i=0; i<texts.length; i++ )
        {
            float y = winY + marginY + ( i*9 );
            if( focusIndex == i )
                Main.screen.fillRect( winX+focusMarginY, y-1, winW-(2*focusMarginY), 7, 5, true );
                
            Main.drawTextCellCentered( winX, y, winW, texts[ i ] );
        }

    }

}

