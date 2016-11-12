package edu.mit.benchmark.b2w;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;


public class TransactionSelector {

    String filename;
    BufferedReader br = null;
    Random r = null;
    int clientId;
    int clientCount;

    private static TransactionSelector singleInstant = null;

    public static TransactionSelector getTransactionSelector(String filename, int clientId, int clientCount) throws FileNotFoundException, IOException {
        if (singleInstant == null)
            singleInstant = new TransactionSelector(filename, clientId, clientCount);
        else if (!singleInstant.getFilename().equals(filename))
            throw new FileNotFoundException("All the filename passed to a TransactionSelector must be the same!");
        return singleInstant;
    }

    public String getFilename(){
        return filename;
    }

    private TransactionSelector(String filename, int clientId, int clientCount) throws FileNotFoundException, IOException {
        r = new Random();
        this.filename = filename;
        this.clientId = clientId;
        this.clientCount = clientCount;

        if(filename==null || filename.isEmpty())
            throw new FileNotFoundException("You must specify a filename to instantiate the TransactionSelector... (probably missing in your workload configuration?)");

        File file = new File(filename);
        FileReader fr = new FileReader(file);
        br = new BufferedReader(fr);

        for(int i = 0; i < clientId; ++i) {
            br.readLine();
        }
    }

    public synchronized JSONObject nextTransaction() throws IOException, JSONException {
        return readNextTransaction();
    }

    private JSONObject readNextTransaction() throws IOException, JSONException {
        String line = br.readLine();
        if(line == null) return null;
        for(int i = 0; i < clientCount-1; ++i) {
            br.readLine();
        }
        return new JSONObject(line);
    }

    public ArrayList<JSONObject> readAll() throws IOException, JSONException {
        ArrayList<JSONObject> transactions = new ArrayList<JSONObject>();

        while (true) {
            JSONObject txn = readNextTransaction();
            if(txn == null) break;
            transactions.add(txn);
        }

        return transactions;
    }

    public void close() throws IOException {
        br.close();
    }

}
