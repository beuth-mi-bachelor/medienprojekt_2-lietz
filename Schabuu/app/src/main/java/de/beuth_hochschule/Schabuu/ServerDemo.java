package de.beuth_hochschule.Schabuu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONObject;

import de.beuth_hochschule.Schabuu.data.ServerConnector;
import de.beuth_hochschule.Schabuu.data.ServerConnectorImplementation;


public class ServerDemo extends Activity {


    // TODO: THIS VARIABLES ARE NORMALLY FROM SETTINGS
    public String playername = "testplayer";

    // EXAMPLE-BUTTONS
    Button getRandomRoom;
    Button switchRoomToTestroom;
    Button switchRoomToTadaroom;


    private ServerConnector _server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_demo);

        // instance of ServerConnector needed
        _server = ServerConnectorImplementation.getInstance("192.168.1.102", 1337);

        /**
         * ESTABLISHING CONNECTION
         */
        // how to connect to Server
        _server.connectToServer(
                // callback for connection established successfully
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        // no arguments given

                        /**
                         * First thing after connection is established once
                         * -> create Player on Server and move him to lobby
                         */
                        _server.initPlayer(playername, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                JSONObject data = (JSONObject) args[0];
                                initDone(data);
                            }
                        });
                    }
                },
                // callback for connection error
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        // no arguments given
                        System.err.println("a connection error occurred");
                    }
                }
        );

        /**
         * EXAMPLE: Get a random room name which is as full as possible
         */
        getRandomRoom = (Button) findViewById(R.id.button_demo_random_room);
        getRandomRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _server.getRandomRoom(new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        // here is the roomName you wanted
                        final String roomName = (String) args[0];

                        // just to display it on device for debugging
                        System.out.println(roomName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), roomName, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        });

        /**
         * EXAMPLE: Switching Room to TestRoom
         */
        switchRoomToTestroom = (Button) findViewById(R.id.button_switch_room);
        switchRoomToTestroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _server.switchRoom("testroom", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final JSONObject data = (JSONObject) args[0];

                        // just to display it on device for debugging
                        System.out.println("room was switched: " + data.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), data.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        /**
         * EXAMPLE: Switching Room to TadaRoom
         */
        switchRoomToTadaroom = (Button) findViewById(R.id.button_switch_to_tada);
        switchRoomToTadaroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _server.switchRoom("tadaroom", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final JSONObject data = (JSONObject) args[0];

                        // just to display it on device for debugging
                        System.out.println("room was switched: " + data.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), data.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }

    public void initDone(JSONObject data) {
        System.out.println(data.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_demo, menu);
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
