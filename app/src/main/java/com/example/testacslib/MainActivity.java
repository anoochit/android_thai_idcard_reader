package com.example.testacslib;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    UsbDevice usbDevice = null;
    int slotNum = 0;
    Reader reader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        reader = new Reader(usbManager);
        Log.d("TAG", "on create");

        for (UsbDevice device : usbManager.getDeviceList().values()) {
            Log.d("TAG", "Found - " + device.getVendorId() + " " + device.getProductId() + " " + device.getDeviceName());
            setText("Found - " + device.getDeviceName());
            if (device.getVendorId() == 1839 && device.getProductId() == 45312) {
                usbDevice = device;
                break;
            }
        }

        // list button
        Button openButton = findViewById(R.id.buttonOpen);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    reader.open(usbDevice);
                    Log.d("TAG", "Connect - " + usbDevice.getDeviceName());
                    setText("Connect - " + usbDevice.getDeviceName());
                    setText("Connect - please insert card");
                } catch (IllegalArgumentException e) {
                    Log.e("TAG", e.getMessage());
                }
            }
        });

        // power card
        Button powerButton = findViewById(R.id.buttonPower);
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // power on card
                try {
                    byte[] atr = reader.power(slotNum, Reader.CARD_WARM_RESET);
                    Log.d("TAG", "Power on");
                    setText("Power on");
                    showByteString(atr);

                } catch (ReaderException e) {
                    Log.d("TAG", "Power error");
                }

                // set state
                try {
                    int protocol = reader.setProtocol(slotNum, Reader.PROTOCOL_T0);
                    Log.d("TAG", "Set protocol - " + protocol);
                    setText("Set protocol - " + protocol);
                } catch (ReaderException e) {
                    Log.d("TAG", "Set protocol - Error");
                }

                try {
                    // get id card info
                    byte[] select = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x08, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x54, (byte) 0x48, (byte) 0x00, (byte) 0x01};
                    byte[] response = new byte[300];
                    int responsLength;
                    responsLength = reader.transmit(slotNum, select, select.length, response, response.length);
                    Log.d("TAG", "Response byte - " + responsLength);
                    setText("Select Card");
                } catch (ReaderException e) {
                    Log.d("TAG", "Error");
                }
            }
        });

        // FIXME : separate fields and clean text message
        // TODO : add photo field
        // read data
        Button readButton = findViewById(R.id.buttonRead);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                byte[] cid = {(byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x04, (byte) 0x02, (byte) 0x00, (byte) 0x0D};
                byte[] cid_getdata = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x0D};

                byte[] person = {(byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x11, (byte) 0x02, (byte) 0x00, (byte) 0xD1};
                byte[] person_getdata = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0xD1};

                byte[] address = {(byte) 0x80, (byte) 0xB0, (byte) 0x15, (byte) 0x79, (byte) 0x02, (byte) 0x00, (byte) 0x64};
                byte[] address_getdata = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64};

                byte[] issue = {(byte) 0x80, (byte) 0xB0, (byte) 0x01, (byte) 0x67, (byte) 0x02, (byte) 0x00, (byte) 0x12};
                byte[] issue_getdata = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x12};


                byte[][] picture = {
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x01, (byte) 0x7B, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x02, (byte) 0x7A, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x03, (byte) 0x79, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x04, (byte) 0x78, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x05, (byte) 0x77, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x06, (byte) 0x76, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x07, (byte) 0x75, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x08, (byte) 0x74, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x09, (byte) 0x73, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x0A, (byte) 0x72, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x0B, (byte) 0x71, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x0C, (byte) 0x70, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x0D, (byte) 0x6F, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x0E, (byte) 0x6E, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x0F, (byte) 0x6D, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x10, (byte) 0x6C, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x11, (byte) 0x6B, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x12, (byte) 0x6A, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x13, (byte) 0x69, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                        {(byte) 0x80, (byte) 0xB0, (byte) 0x14, (byte) 0x68, (byte) 0x02, (byte) 0x00, (byte) 0xFF},
                };

                byte[] picture_getdata = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0xFF};


                byte[] response = new byte[500];
                int responsLength, index = 0;

                try {
                    // cid
                    reader.transmit(slotNum, cid, cid.length, response, response.length);
                    responsLength = reader.transmit(slotNum, cid_getdata, cid_getdata.length, response, response.length);
                    Log.d("TAG", "Response byte - " + responsLength);
                    Log.d("TAG", byteArrayToHexString(response, 0, responsLength));

                    // person
                    reader.transmit(slotNum, person, person.length, response, response.length);
                    responsLength = reader.transmit(slotNum, person_getdata, person_getdata.length, response, response.length);
                    Log.d("TAG", "Response byte - " + responsLength);
                    Log.d("TAG", byteArrayToHexString(response, 0, responsLength-1));

                    // adddress
                    reader.transmit(slotNum, address, address.length, response, response.length);
                    responsLength = reader.transmit(slotNum, address_getdata, address_getdata.length, response, response.length);
                    Log.d("TAG", "Response byte - " + responsLength);
                    Log.d("TAG", byteArrayToHexString(response, 0, responsLength));

                    // issue
                    reader.transmit(slotNum, issue, issue.length, response, response.length);
                    responsLength = reader.transmit(slotNum, issue_getdata, issue_getdata.length, response, response.length);
                    Log.d("TAG", "Response byte - " + responsLength);
                    Log.d("TAG", byteArrayToHexString(response, 0, responsLength));

                    // image
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    for (int i = 0; i < picture.length; i++) {
                        reader.transmit(slotNum, picture[i], picture[i].length, response, response.length);
                        responsLength = reader.transmit(slotNum, picture_getdata, picture_getdata.length, response, response.length);
                        Log.d("TAG", "Response byte - " + i + " - " + responsLength);
                        buffer.write(response, 0, responsLength - 2);
                        index += responsLength - 2;
                    }

                    Log.d("TAG", "buffer size - " + buffer.size());
                    //Log.d("TAG", "buffer - " + buffer.toString());

                    byte[] pictureBuffer;
                    pictureBuffer = buffer.toByteArray();
                    while (index >= 0 && pictureBuffer[index - 1] == 0x20) index--;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(pictureBuffer, 0, index);
                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);

                } catch (ReaderException e) {
                    Log.d("TAG", "Error");
                }

            }
        });

        // close card
        Button closeButton = findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textMessage = findViewById(R.id.textMessage);
                textMessage.setText("");
                reader.close();

            }
        });


        reader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
            @Override
            public void onStateChange(int _slotNum, int _prevState, int _currState) {
                Log.d("TAG", "State -> change listener");
                Log.d("TAG", "slotNum -> " + _slotNum);
                Log.d("TAG", "prevState -> " + _prevState);
                Log.d("TAG", "currState -> " + _currState);
                if (_prevState == 1 && _currState==2) {
                    Log.d("TAG", "state - card inserted");
                }
                if (_prevState == 2 && _currState==1) {
                    Log.d("TAG", "state - card removed");
                }
                slotNum = _slotNum;
            }
        });


    }

    String showByteString(byte[] input) {
        StringBuilder output;
        if (input == null) {
            Log.d("TAG", "Empty");
        }
        output = new StringBuilder();
        for (byte b : input) {

            output.append(String.format("%02x", b));
        }
        Log.d("TAG", "Byte String - " + output.toString());

        String result = null;
        try {
            result = new String(input, "TIS620");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("TAG", "Cannot parse!");
        }
        TextView textMessage = findViewById(R.id.textMessage);
        String text = ((String) textMessage.getText()) + "\n" + result;
        textMessage.setText(text);
        return result;
    }

    void setText(String text) {
        TextView textMessage = findViewById(R.id.textMessage);
        String result = ((String) textMessage.getText()) + "\n" + text;
        textMessage.setText(result);
    }

    String byteArrayToHexString(byte[] input, int index, int length) {
        byte[] selectBytes;

        if ((length + index) > input.length) {
            length = input.length - index;
        }

        selectBytes = new byte[length];
        System.arraycopy(input, index, selectBytes, 0, length - 2);

        return showByteString(selectBytes);
    }

    /*
    // get data by send only 1st apdu command
    void getData(byte[] command) {
        byte[] getData = {(byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00};
        byte[] getDataCmd = Arrays.copyOf(getData, getData.length + 1);
        System.arraycopy(command, (command.length - 1), getDataCmd, getData.length, 1);

        byte[] response = new byte[500];
        int responsLength;

        try {
            reader.transmit(slotNum, command, command.length, response, response.length);
            responsLength = reader.transmit(slotNum, getDataCmd, getDataCmd.length, response, response.length);
            Log.d("TAG", "Response byte - " + responsLength);
            Log.d("TAG", byteArrayToHexString(response, 0, responsLength));
        } catch (ReaderException e) {
            Log.d("TAG", "ERROR");
        }

    }
    */



}