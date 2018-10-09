package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class HTMLTextView extends AppCompatTextView implements Html.ImageGetter {
    public HTMLTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param source HTML 형식의 문자열
     */
    public void setHtmlText(String source) {

        Spanned spanned = Html.fromHtml(source, this, null);    // Html.ImageGetter 를 여기다 구현해놨다.
        this.setText(spanned);

    }

    /**
     * Html.ImageGetter 구현.
     *
     * @param source <img> 태그의 주소가 넘어온다.
     * @return 일단 LevelListDrawable 을 넘겨줘서 placeholder 처럼 보여주고, AsyncTask 를 이용해서 이미지를 다운로드
     */
    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable list_d = new LevelListDrawable();

        // 이미지 다운로드 전, 임시 이미지 출력
        Drawable loading = ContextCompat.getDrawable(getContext(), R.drawable.loading);
        list_d.addLevel(0, 0, loading);
        list_d.setBounds(0, 0, loading.getIntrinsicWidth(), loading.getIntrinsicHeight());

        new LoadImage().execute(source, list_d);
        return list_d;
    }

    /**
     * 실제 온라인에서 이미지를 다운로드 받을 AsyncTask
     */
    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];

            // OKKY 내부 서버저장 파일인 경우 경로를 https:// 로 바꿔준다
            if (source != null && source.charAt(0) == '/' && source.charAt(1) == '/')
                source = source.replace("//", "https://");

            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
                /*
                 * 4096x4096 사이즈 이상의 이미지는 너무 커서 OpenGL을 통한 텍스쳐로 로드되지 않는다.
                 * 그렇기 때문에 이미지를 리사이즈 해야하는데, 이 경우 이미지의 화질이 하락할 가능성이 있다.
                 *
                 * 반면에 Manifest 에서 OpenGL 의 하드웨어 가속을 꺼두면 4096 사이즈 이상의 이미지도 로드할 수 있다.
                 * 이 경우, 사용자의 데이터 소모량 증가 및 퍼포먼스 하락이 발생할 수 있다. (현재는 이 방법 사용)
                 * */
//                return resizeBitmapImage(bitmap, 4096);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("에러", "파일못찾음");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("에러", "경로주소잘못됨 : " + source);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("에러", "IO 예외발생");
            }
            return null;
        }

        /**
         * 이미지 다운로드가 완료되면, 처음에 placeholder 처럼 만들어서 사용하던 Drawable 에, 다운로드 받은 이미지를 넣어준다.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(getContext().getResources(), bitmap);

                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, getWidth(), (bitmap.getHeight() * getWidth()) / bitmap.getWidth());
                mDrawable.setLevel(1);

                // 이미지 다운로드 완료 후, invalidate 의 개념으로, 다시한번 텍스트를 설정해준것이다. 더 좋은방법이 있을법도 하다
                CharSequence t = getText();
                setText(t);
            }
        }

        public Bitmap resizeBitmapImage(Bitmap source, int maxResolution) {
            int width = source.getWidth();
            int height = source.getHeight();
            int newWidth = width;
            int newHeight = height;
            float rate = 0.0f;
            if (width > height) {
                if (maxResolution < width) {
                    rate = maxResolution / (float) width;
                    newHeight = (int) (height * rate);
                    newWidth = maxResolution;
                }
            } else {
                if (maxResolution < height) {
                    rate = maxResolution / (float) height;
                    newWidth = (int) (width * rate);
                    newHeight = maxResolution;
                }
            }
            return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
        }
    }
}
