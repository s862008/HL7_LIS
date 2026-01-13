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
        String[] fields = line.split("\\|", -1); // Используем -1 для сохранения пустых полей
        String segmentName = line.substring(0, 3);

        switch (segmentName) {
            case "MSH":
                parseMSH(fields);
                break;
            case "PID":
                parsePID(fields);
                break;
            case "PV1":
                parsePV1(fields);
                break;
            case "ORC":
                parseORC(fields);
                break;
            case "OBR":
                parseOBR(fields);
                break;
            case "OBX":
                parseOBX(fields);
                break;
            case "NTE":
                parseNTE(fields);
                break;
            case "DG1":
                parseDG1(fields);
                break;
            case "IN1":
                parseIN1(fields);
                break;
            case "SPM":
                parseSPM(fields);
                break;
            case "NK1":
                parseNK1(fields);
                break;
                case "ERR":
                parseERR(fields);
                break;
            case "DSC":
                parseDSC(fields);
                break;
            default:
                System.err.println("Неизвестный сегмент: " + segmentName);
                break;
        }
    }

    private void parseMSH(String[] fields) {
        try {
            System.out.println("NEW MESSAGE: ");
            if (fields.length > 1) {
                System.out.println("\tSending Application: " + fields[2]);
                System.out.println("\tSending Facility: " + fields[3]);
                System.out.println("\tReceiving Application: " + fields[4]);
                System.out.println("\tReceiving Facility: " + fields[5]);
                System.out.println("\tDate/Time of Message: " + fields[6]);
                System.out.println("\tMessage Type: " + fields[8]);
                System.out.println("\tMessage Control ID: " + fields[9]);
                if (fields.length > 10) {
                    System.out.println("\tProcessing ID: " + fields[10]);
                    System.out.println("\tVersion ID: " + fields[11]);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента MSH");
        }
        this.result = null;
    }

    private void parsePID(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("PATIENT INFORMATION:");
                System.out.println("\tPatient ID: " + (fields.length > 2 ? fields[2] : ""));
                System.out.println("\tAlternate Patient ID: " + (fields.length > 3 ? fields[3] : ""));
                System.out.println("\tPatient Name: " + (fields.length > 5 ? fields[5] : ""));
                System.out.println("\tDate/Time of Birth: " + (fields.length > 7 ? fields[7] : ""));
                System.out.println("\tSex: " + (fields.length > 8 ? fields[8] : ""));
                System.out.println("\tPatient Address: " + (fields.length > 11 ? fields[11] : ""));
                System.out.println("\tPhone Number: " + (fields.length > 12 ? fields[12] : ""));
                System.out.println("\tSSN: " + (fields.length > 19 ? fields[19] : ""));
            } else {
                throw new IllegalStateException();
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalStateException e) {
            System.err.println("Внимание! неполное сообщение сегмента PID");
        }
    }

    private void parsePV1(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("PATIENT VISIT:");
                System.out.println("\tVisit Number: " + (fields.length > 19 ? fields[19] : ""));
                System.out.println("\tPatient Class: " + (fields.length > 2 ? fields[2] : ""));
                System.out.println("\tAssigned Location: " + (fields.length > 3 ? fields[3] : ""));
                System.out.println("\tAdmission Type: " + (fields.length > 4 ? fields[4] : ""));
                System.out.println("\tAttending Doctor: " + (fields.length > 7 ? fields[7] : ""));
                System.out.println("\tReferring Doctor: " + (fields.length > 8 ? fields[8] : ""));
                System.out.println("\tFinancial Class: " + (fields.length > 20 ? fields[20] : ""));
                System.out.println("\tAdmission Date/Time: " + (fields.length > 44 ? fields[44] : ""));
            } else {
                throw new IllegalStateException();
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalStateException e) {
            System.err.println("Внимание! неполное сообщение сегмента PV1");
        }
    }

    private void parseORC(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("ORDER CONTROL:");
                System.out.println("\tOrder Control: " + fields[1]);
                System.out.println("\tPlacer Order Number: " + (fields.length > 2 ? fields[2] : ""));
                System.out.println("\tFiller Order Number: " + (fields.length > 3 ? fields[3] : ""));
                System.out.println("\tOrdering Provider: " + (fields.length > 12 ? fields[12] : ""));
                System.out.println("\tOrder Status: " + (fields.length > 5 ? fields[5] : ""));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента ORC");
        }
    }

    private void parseOBR(String[] fields) {
        try {
            if (fields.length > 32) {
                System.out.println("OBSERVATION REQUEST:");
                System.out.println("\tPlacer Order Number: " + fields[2]);
                System.out.println("\tFiller Order Number: " + fields[3]);
                System.out.println("\tUniversal Service ID: " + fields[4]);
                System.out.println("\tPriority: " + fields[5]);
                System.out.println("\tRequested Date/Time: " + fields[6]);
                System.out.println("\tObservation Date/Time: " + fields[7]);
                System.out.println("\tOrdering Provider: " + fields[16]);
                System.out.println("\tResult Status: " + fields[25]);
                
                if (fields[4] != null && !fields[4].isEmpty()) {
                    this.result = new Result(fields[4]);
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalStateException e) {
            System.err.println("Внимание! неполное сообщение сегмента OBR");
        }
    }

    private void parseOBX(String[] fields) {
        try {
            if (this.result != null) {
                String observIdent = fields.length > 3 ? fields[3] : "";
                String observValue = fields.length > 5 ? fields[5] : "";
                String observUnits = fields.length > 6 ? fields[6] : "";
                String references = fields.length > 7 ? fields[7] : "";
                String flags = fields.length > 8 ? fields[8] : "";
                String observDate = fields.length > 14 ? fields[14] : "";

                if (!observIdent.isEmpty()) {
                    result.putValue(observIdent, observValue);
                    result.putNormal(observIdent, references);
                    result.putFlag(observIdent, flags);
                    result.putUnits(observIdent, observUnits);
                    
                    System.out.println("OBSERVATION: " + observIdent + " = " + observValue + 
                                     " " + observUnits + " (" + flags + ")");
                }
            } else {
                System.err.println("Внимание! нет данных по номеру заказа или сегмент OBR не обработан");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента OBX");
        }
    }

    private void parseNTE(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("NOTES AND COMMENTS:");
                System.out.println("\tComment Source: " + fields[1]);
                System.out.println("\tComment: " + (fields.length > 3 ? fields[3] : ""));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента NTE");
        }
    }

    private void parseDG1(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("DIAGNOSIS:");
                System.out.println("\tDiagnosis Code: " + (fields.length > 3 ? fields[3] : ""));
                System.out.println("\tDiagnosis Description: " + (fields.length > 4 ? fields[4] : ""));
                System.out.println("\tDiagnosis Type: " + (fields.length > 6 ? fields[6] : ""));
                System.out.println("\tDiagnosing Provider: " + (fields.length > 16 ? fields[16] : ""));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента DG1");
        }
    }

    private void parseIN1(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("INSURANCE:");
                System.out.println("\tInsurance Company ID: " + (fields.length > 3 ? fields[3] : ""));
                System.out.println("\tInsurance Company Name: " + (fields.length > 4 ? fields[4] : ""));
                System.out.println("\tPlan Type: " + (fields.length > 2 ? fields[2] : ""));
                System.out.println("\tPolicy Number: " + (fields.length > 36 ? fields[36] : ""));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента IN1");
        }
    }

    private void parseSPM(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("SPECIMEN:");
                System.out.println("\tSpecimen ID: " + (fields.length > 2 ? fields[2] : ""));
                System.out.println("\tSpecimen Type: " + (fields.length > 4 ? fields[4] : ""));
                System.out.println("\tSpecimen Source: " + (fields.length > 6 ? fields[6] : ""));
                System.out.println("\tCollection Date/Time: " + (fields.length > 17 ? fields[17] : ""));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента SPM");
        }
    }

    private void parseNK1(String[] fields) {
        try {
            if (fields.length > 1) {
                System.out.println("NEXT OF KIN:");
                System.out.println("\tName: " + (fields.length > 2 ? fields[2] : ""));
                System.out.println("\tRelationship: " + (fields.length > 3 ? fields[3] : ""));
                System.out.println("\tAddress: " + (fields.length > 4 ? fields[4] : ""));
                System.out.println("\tPhone: " + (fields.length > 5 ? fields[5] : ""));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента NK1");
        }
    }
    private void parseERR(String[] fields) {
        try {
            System.err.println("ERROR SEGMENT DETECTED:");
            if (fields.length > 1) {

                String errorCode = fields.length > 3 ? fields[3] : "";
                String severity = fields.length > 4 ? fields[4] : "";
                String errorMessage = fields.length > 8 ? fields[8] : "";
                String diagnosticInfo = fields.length > 7 ? fields[7] : "";

                System.err.println("\tError Code: " + errorCode);
                System.err.println("\tSeverity: " + getErrorSeverity(severity));
                System.err.println("\tMessage: " + errorMessage);
                System.err.println("\tDiagnostic: " + diagnosticInfo);

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента ERR");
        }
    }

    private void parseDSC(String[] fields) {
        try {
            System.out.println("CONTINUATION POINTER:");
            if (fields.length > 1) {

                String continuationPointer = fields[1];
                String continuationStyle = fields.length > 2 ? fields[2] : "";

                System.out.println("\tContinuation Pointer: " + continuationPointer);
                System.out.println("\tContinuation Style: " + continuationStyle);

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Внимание! неполное сообщение сегмента DSC");
        }
    }

    private String getErrorSeverity(String severityCode) {
        switch (severityCode) {
            case "I":
                return "Information";
            case "W":
                return "Warning";
            case "E":
                return "Error";
            case "F":
                return "Fatal Error";
            default:
                return "Unknown";
        }
    }
}


}
