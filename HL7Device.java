package lis.hl7devices;

import java.util.ArrayList;
import lis.AbstractDevice;
import lis.Result;

public abstract class HL7Device extends AbstractDevice {

    private Result result;
    ArrayList<String> MessagesFromAnl = new ArrayList<>();

    public HL7Device(Communicator c, Connection connection, int idDevice) {
        super(c, connection, idDevice);
    }

    @Override
    public void Processing(String s) {

        if (s.equals(AbstractDevice.CONTROL_CHARACTERS[13])) {
            if (temp != null && temp.length() != 0) {
                MessagesFromAnl.add(temp.toString());
                temp.setLength(0);
            }

            setChanged();
            notifyObservers("\r\n");
            return;
        }

        // ловим  FS, конец сообшения
        if (s.equals(AbstractDevice.CONTROL_CHARACTERS[28])) {
            parseData(MessagesFromAnl);
            mSaveResults(result);
            temp = null;
            return;

        }

        // ловим  LT, начало сообшения
        if (s.equals(AbstractDevice.CONTROL_CHARACTERS[11])) {
            MessagesFromAnl.clear();
            temp = new StringBuffer(0);
            return;
        }

        if (temp != null) {
            temp.append(s);
        }
    }

  protected void parseData(ArrayList<String> data) {

        for (String line : data) {
            parseHL7(line);
        }
    }

 protected void parseHL7(String line) {

        String[] fields = line.split("\\|");
        String segmentName = line.substring(0, 3); //fields[0]

        switch (segmentName) {
            case "PID":
                if (fields.length > 8) {
                    //System.out.println("ID: " + fields[1]);
                    //System.out.println("Patient ID: " + fields[2]);
                    System.out.println("Patient Name: " + fields[5]);
                    System.out.println("Date/Time of Birth: " + fields[7]);
                    System.out.println("Sex: " + fields[8]);
                } else {
                    System.err.println("Внимание! не полное сообшение сегмента PID");
                }
                break;
            case "PV1":
                if (fields.length > 20) {
                    //System.out.println("ID-PV1: " + fields[1]);
                    //System.out.println("Patient Class: " + fields[2]);
                    //System.out.println("Financial Class: " + fields[20]);
                } else {
                    System.err.println("Внимание! не полное сообшение сегмента PV1");
                }
                break;
            case "OBR":
                if (fields.length > 32) {
                    //System.out.println("ID-OBR: " + fields[1]);
                    System.out.println("ExamID: " + fields[3]);
                    this.result = new Result(fields[3]);
                } else {
                    System.err.println("Внимание! не полное сообшение сегмента OBR");
                }
                break;
            case "OBX":
                if (this.result != null) {

                    String observIdent = fields[3];
                    String observValue = fields[5];
                    String references = fields[7];
                    String flags = fields[8];

                    if (!(observIdent.isEmpty())) {
                        result.putValue(observIdent, observValue);
                        result.putNormal(observIdent, references);
                        result.putFlag(observIdent, flags);
                    }

                } else {
                    System.err.println("Внимание! нет данных по номеру заказа или сегмент OBR не обработан");
                }
                break;
            case "MSH":
                System.out.println("NEW MASSAGE: ");
                if (fields.length > 18) {
                    System.out.println("\\tDate/Time of Message: " + fields[7]);
                    System.out.println("\\tMessage Type: " + fields[9]);
                }

                this.result = null;
                break;
        }

    }


}
