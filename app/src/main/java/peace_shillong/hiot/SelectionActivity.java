package peace_shillong.hiot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.IOException;
import java.util.List;
import peace_shillong.model.BookNavigator;
import peace_shillong.model.DatabaseManager;

public class SelectionActivity extends AppCompatActivity {

    private Spinner spinnerChapters;
    private Spinner spinnerBooks;
    private Spinner spinnerVerses;
    private SQLiteDatabase database;
    private BookNavigator navigator;
    private String book;
    private int chapter;
    private int verse;
    private Button buttonGo;
    private TextView permissionText;

    private SQLiteDatabase initializeDatabase(Context context) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
        SQLiteDatabase database = null;

        try {
            dataBaseHelper.createDataBase();
            database = dataBaseHelper.getReadableDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return database;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        //request permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Log.d("Load","Permission");
            permissionText=findViewById(R.id.textView_permission);
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                            //Permissions Granted
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                            /*Permissions not granted*/
                            permissionText.setVisibility(View.VISIBLE);
                        }
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        }


        buttonGo = findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("book", book);
                bundle.putInt("chapter", chapter);
                bundle.putInt("verse", verse);
                i.putExtras(bundle);

                startActivity(i);
            }
        });

        DatabaseManager.init(this);
        DatabaseManager instance = DatabaseManager.getInstance();

        navigator = new BookNavigator(database);

        spinnerBooks = (Spinner) findViewById(R.id.spinnerbooks);
        spinnerVerses = (Spinner) findViewById(R.id.spinnerverses);
        spinnerChapters = (Spinner)findViewById(R.id.spinnerchapters);

        String[] books = navigator.getBooks();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, books);
        spinnerBooks.setAdapter(adapter);

        spinnerBooks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);

                book = (String)itemAtPosition;

                if (view != null) {
                    List<String> list = navigator.getChapters(book);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerChapters.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerChapters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);

                try {
                    chapter = Integer.parseInt(itemAtPosition.toString());
                } catch(NumberFormatException exception) {
                    exception.printStackTrace();
                    return;
                }

                List<String> list = navigator.getVerses(book, chapter);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(parent.getContext(), android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVerses.setAdapter(dataAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerVerses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);

                try {
                    verse = Integer.parseInt((String)itemAtPosition);
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
