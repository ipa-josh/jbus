/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package HWDriver.AVRNETIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bibliothek für Pollin AVR NetIO mit Standardfirmware
 *
 * @version 1.0
 */
public class AvrNetIo 
{

    private Socket client;
    private String ip;
    private int port;

    /**
     * Konstruktor
     * @param IP des NET-IO Boards
     * @param port Port des Net-IO Boards ( Standard 50290)
     */
    public AvrNetIo(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Stellt Verbindung zum NET-IO Board her
     * Timeout 10000ms
     * @return true: Verbindung hergestellt, false Fehler
     */
    public boolean connect()
    {
        boolean connected = false;
        try {

            client = new Socket();
            client.connect(new InetSocketAddress(ip, port), 10000);
            connected = true;

        } catch (UnknownHostException ex) {
            Logger.getLogger(AvrNetIo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AvrNetIo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connected;
    }

    /**
     * Disconnect Methode
     */
    public void disconnect()
    {
        try {
            client.close();
        } catch (IOException ex) {
            Logger.getLogger(AvrNetIo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gibt den Verbindungsstatus zurück
     * @return Status
     */
    public boolean isConnected()
    {
        return client.isConnected();
    }


    /**
     * Schickt Daten zum Board
     * @param msg Daten
     * @return Antwort
     */
    private String sendData(String msg)
    {
        String line = "";
        if(isConnected())
        {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println(msg+"\r\n");
                out.flush();
                line = in.readLine();
                line = line.replaceAll("\0", "").replaceAll("\r\n", "").trim();
            } catch (IOException ex) {
                Logger.getLogger(AvrNetIo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return line;
    }

    /**
     * Gibt den Wert eines ADC-Port zurück (ADC Port (1-4))
     * @param port Port
     * @return ADC Wert (-1 : Fehler)
     */
    public int getADC(int port)
    {
        if(port < 1 || port > 4)
        {
            return -1;
        }

        return Integer.parseInt(sendData("GETADC "+port));
    }

    /**
     * Setzt den Wert eines Ausgangsports
     * @param port Port (1-8)
     * @param level true= high, false = low
     * @return true = ACK, false = NAK oder Fehler
     */
    public boolean setOutPort(int port, boolean level)
    {
        if(port < 1 || port > 8)
        {
            return false;
        }
        char levelchar = level ? '1' : '0';
        return sendData("SETPORT "+port+"."+levelchar).equals("ACK");
    }

    /**
     * Gibt den Wert eines Ausgangs zurück (1-8)
     * @param port Port (1-8)
     * @return -1 = Fehler, 1 = high, 0 = low
     */
    public int getOutPort(int port)
    {

        if(port <  1 || port > 8)
        {
            return -1;
        }

        String answer = sendData("GETSTATUS");

        if(answer.length() > 1)
        {
            return Integer.parseInt(""+answer.charAt(port));
        }
        return -1;
    }

    /**
     * Liefert das Ergebnis von GETSTATUS zurück
     * @return S******** *= Portwert ( 1 oder 0)
     */
    public String getStatus()
    {
        return sendData("GETSTATUS");
    }

    /**
     * Liefert das Ergebnis von GETSTATUS zurück
     * @return S******** *= Portwert ( 1 oder 0)
     */
    public boolean getPort(int port)
    {
        if(port <  1 || port > 4)
        {
            return false;
        }
        
        return !sendData("GETPORT "+port).equals("0");
    }

    /**
     * Gibt die IP des Boards zurück
     * @return IP
     */
    public String getIP()
    {
        return sendData("GETIP");
    }

    /**
     * Setzt die IP des Boards
     * Reset des Boards notwendig
     * @param ip IP (xxx.xxx.xxx.xxx)
     * @return true = ACK , false = NAK
     */
    public boolean setIP(String ip)
    {
        return sendData("SETIP "+ip).equals("ACK");
    }

    /**
     * Liefert die Netzmaske des Boards zurück
     * @return Netzmaske (xxx.xxx.xxx.xxx)
     */
    public String getMask()
    {
        return sendData("GETMASK");
    }

    /**
     * Setzt die Netzmaske des Boards
     * @param mask Neue Maske (xxx.xxx.xxx.xxx)
     * @return true = ACK , false = NAK
     */
    public boolean setMask(String mask)
    {
        return sendData("SETMASK "+mask).equals("ACK");
    }


    /**
     * Gibt den Gateway des Boards zurück
     * @return Gateway (xxx.xxx.xxx.xxx)
     */
    public String getGW()
    {
        return sendData("GETGW");
    }

    /**
     * Setzt den Gateway
     * @param gw (xxx.xxx.xxx.xxx)
     * @return true = ACK , false = NAK
     */
    public boolean setGW(String gw)
    {
        return sendData("SETGW "+gw).equals("ACK");
    }

    /**
     * Initialisiert das LCD
     * @return true = ACK , false = NAK
     */
    public boolean initLCD()
    {
        return sendData("INITLCD").equals("ACK");
    }

    /**
     * Schreibt einen Text auf das LCD in eine bestimmte Zeile
     * @param line Zeile (1 oder 2)
     * @param txt Text der geschrieben werden soll
     * @return true = ACK , false = NAK oder Fehler
     */
    public boolean writeLCD(int line, String txt)
    {
        boolean answer = false;
        if(line == 1 || line == 2)
        {
            answer = sendData("WRITELCD "+line+"."+txt).equals("ACK");
        }
        return answer;
    }

    /**
     * Löscht das LCD
     * @param line Zeilennummer ( 1 oder 2 , sonst alle)
     */
    public void clearLCD(int line)
    {
        if(line == 1 || line == 2)
        {
            sendData("CLEARLCD "+line);
        }
        else
        {
            sendData("CLEARLCD");
        }
    }

    /**
     * Gibt die Version zurück
     * @return Version
     */
    public String getVersion()
    {
        return sendData("VERSION");
    }




}
