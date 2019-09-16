import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class TimeMeterEntity extends TimeMeter 
{
   
   void drawMeter(HiRes16Color screen, float normalizedTimeLeft)
   {
       //screen.fillRect( 0, 176 - height()-2, 220, height() + 2, 3 );
       int endX = (int)((float)(220 - 2) * normalizedTimeLeft);
       for(int posX = 0; posX + width() < endX; posX += width() )
       {
           draw( screen, posX+1, 176 - height() - 1, false, false, true );
       }
   }
   
}

