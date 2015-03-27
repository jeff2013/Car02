package com.example.jeff.car02;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class Login extends Activity {
    Button redirect;
    Bitmap bitmap;

    public boolean onTouch(View v, MotionEvent event) {

        int eventPadTouch = event.getAction();
        float iX=event.getX();
        float iY=event.getY();

        switch (eventPadTouch) {

            case MotionEvent.ACTION_DOWN:
                if (iX>=0 & iY>=0 & iX<bitmap.getWidth() & iY<bitmap.getHeight()) { //Makes sure that X and Y are not less than 0, and no more than the height and width of the image.
                    if (bitmap.getPixel((int) iX, (int) iY)!=0) {
                        Toast.makeText(this, "Bitmapclicked", Toast.LENGTH_SHORT).show();
                        // actual image area is clicked(alpha not equal to 0), do something
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* MainActivity m = new MainActivity();
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.buttongraphic);
        Display display  = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        bitmap = getResizedBitmap(bitmap, height, width);
        View mainView = View.inflate(Login.this, R.layout.activity_login2, null);
        redirect = (Button) mainView.findViewById(R.id.btn_login);
        redirect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventPadTouch = event.getAction();
                float iX=event.getX();
                float iY=event.getY();

                switch (eventPadTouch) {

                    case MotionEvent.ACTION_DOWN:
                        if (iX>=0 & iY>=0 & iX<bitmap.getWidth() & iY<bitmap.getHeight()) { //Makes sure that X and Y are not less than 0, and no more than the height and width of the image.
                            if (bitmap.getPixel((int) iX, (int) iY)!=0) {
                                Toast.makeText(Login.this, "Bitmapclicked", Toast.LENGTH_SHORT).show();
                                // actual image area is clicked(alpha not equal to 0), do something
                            }
                        }
                        return true;
                }
                return false;
            }
        });
        */
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
            // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }






    private void launchMainActivity(){
        Intent mainActivity = new Intent(Login.this, MainActivity.class);
        startActivity(mainActivity);
    }
}