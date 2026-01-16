package lis.hl7devices;

import java.util.ArrayList;
import lis.AbstractDevice;
import lis.Result;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class HL7Device extends AbstractDevice {
   
    private static final char CARRIAGE_RETURN = (char) 13;
    private static final char START_BLOCK = (char) 11;
    private static final char END_BLOCK = (char) 28;

    private Result result;
    private String originalControlId = "";
    private String messageType = "";
    private String sendingApplication = "";
    private String sendingFacility = "" ;
    private Boolean isMessageComplete = false;
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
           
            if (isMessageComplete) {
                processMessageComplete();
            }
 
            return;
        }

        // ловим  FS, конец сообшения
        if (s.equals(AbstractDevice.CONTROL_CHARACTERS[28])) {
            isMessageComplete = true;
            temp = null;
            return;

        }

        // ловим  LT, начало сообшения
        if (s.equals(AbstractDevice.CONTROL_CHARACTERS[11])) {
            MessagesFromAnl.clear();
            isMessageComplete = false;
            temp = new StringBuffer(0);
            return;
        }

        if (temp != null) {
            temp.append(s);
        }
    }
 
   private void processMessageComplete() {
         if (!MessagesFromAnl.isEmpty()) {
                // Разбор сообшения по сегментно
                parseData(MessagesFromAnl);
                 // Валидация и отправка  ACK c ошибкой
                if (originalControlId.isEmpty()) {
                    sendACK("AR", "", "Missing MSH segment", "100");
                    return;
                }
                // Успешный ACK
                sendACK("AA", originalControlId, null, null);
                // Сохранение полученных данных
                mSaveResults(result); 
                
                isMessageComplete = false;
                MessagesFromAnl.clear();
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
            case "QRD":
                parseQRD(fields);
                break;
            case "QRF":
                parseQRF(fields);
                break;
            case "QPD":
                parseQPD(fields);
                break;
            case "RCP":
                parseRCP(fields);
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
                sendingApplication = fields[2];
                System.out.println("\tSending Facility: " + fields[3]);
                sendingFacility = fields[3]; 
                System.out.println("\tReceiving Application: " + fields[4]);
                System.out.println("\tReceiving Facility: " + fields[5]);
                System.out.println("\tDate/Time of Message: " + fields[6]);
                System.out.println("\tMessage Type: " + fields[8]);
                messageType = fields[8];
                System.out.println("\tMessage Control ID: " + fields[9]);
                messageControlId = fields[9];
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
    // Методы для парсинга этих сегментов:
protected void parseQRD(String[] fields) {
    try {
        System.out.println("QUERY DEFINITION (QRD - Legacy):");
        if (fields.length > 1) {
            System.out.println("\tQuery Date/Time: " + fields[1]);
            System.out.println("\tQuery Format: " + fields[2]);
            System.out.println("\tQuery Priority: " + fields[3]);
            System.out.println("\tQuery ID: " + fields[4]);
            
            if (fields.length > 8) {
                System.out.println("\tPatient Filter: " + fields[8]);
            }
        }
    } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("Внимание! неполное сообщение сегмента QRD");
    }
}

protected void parseQRF(String[] fields) {
    try {
        System.out.println("QUERY FILTER (QRF - Legacy):");
        if (fields.length > 1) {
            System.out.println("\tWhere Subject Filter: " + fields[1]);
            System.out.println("\tStart Date/Time: " + (fields.length > 2 ? fields[2] : ""));
            System.out.println("\tEnd Date/Time: " + (fields.length > 3 ? fields[3] : ""));
        }
    } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("Внимание! неполное сообщение сегмента QRF");
    }
}

protected void parseQPD(String[] fields) {
    try {
        System.out.println("QUERY PARAMETER DEFINITION:");
        if (fields.length > 1) {
            System.out.println("\tMessage Query Name: " + fields[1]);
            System.out.println("\tQuery Tag: " + fields[2]);
            
            // Параметры запроса начинаются с поля 3
            for (int i = 3; i < fields.length; i++) {
                if (!fields[i].isEmpty()) {
                    System.out.println("\tParameter " + (i-2) + ": " + fields[i]);
                }
            }
        }
    } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("Внимание! неполное сообщение сегмента QPD");
    }
}

protected void parseRCP(String[] fields) {
    try {
        System.out.println("RESPONSE CONTROL PARAMETERS:");
        if (fields.length > 1) {
            System.out.println("\tQuery Priority: " + fields[1]);
            System.out.println("\tQuantity Limited Request: " + (fields.length > 2 ? fields[2] : ""));
            System.out.println("\tResponse Modality: " + (fields.length > 3 ? fields[3] : ""));
            System.out.println("\tExecution and Delivery Time: " + (fields.length > 4 ? fields[4] : ""));
            System.out.println("\tModify Indicator: " + (fields.length > 5 ? fields[5] : ""));
        }
    } catch (ArrayIndexOutOfBoundsException e) {
        System.err.println("Внимание! неполное сообщение сегмента RCP");
    }
}
    // Метод отправки ACK
    private void sendACK(String ackCode, String originalMessageId, 
                        String errorMessage, String errorCode) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            
            // Генерация уникального ID для ACK
            String ackMessageId = "ACK_" + System.currentTimeMillis();
            
            // Построение сообщения ACK
            StringBuilder ackMessage = new StringBuilder();
            
            // MSH сегмент
            ackMessage.append("MSH|^~\\&|")
                      .append("LIS|") // Receiving Application
                      .append("LIS_FACILITY|") // Receiving Facility
                      .append(sendingApplication).append("|")
                      .append(sendingFacility).append("|")
                      .append(timestamp).append("||")
                      .append("ACK^R01|")
                      .append(ackMessageId).append("|")
                      .append("P|2.4||||||")
                      .append("UTF-8")
                      .append(CARRIAGE_RETURN);
            
            // MSA сегмент
            ackMessage.append("MSA|")
                      .append(ackCode).append("|")
                      .append(originalMessageId);
            
            if (errorMessage != null) {
                ackMessage.append("|").append(errorMessage);
            }
            
            if (errorCode != null) {
                ackMessage.append("|||").append(errorCode);
            }
            
            ackMessage.append(CARRIAGE_RETURN);
            
            // Отправка через MLLP
            byte[] messageBytes = ackMessage.toString().getBytes("ISO-8859-1");
            
            // Добавляем MLLP envelope
            ByteArrayOutputStream mllpMessage = new ByteArrayOutputStream();
            mllpMessage.write(START_BLOCK);
            mllpMessage.write(messageBytes);
            mllpMessage.write(END_BLOCK);
            mllpMessage.write(CARRIAGE_RETURN);
            
            // Отправка
            comm.Write(mllpMessage.toByteArray());
            
            System.out.println("Sent ACK: " + ackCode + " for message " + originalMessageId);
            
        } catch (Exception e) {
            System.err.println("Error sending ACK: " + e.getMessage());
        }
    }

}

