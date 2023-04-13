CREATE TABLE "${schema}"."RENDICONTAZIONE_SFTP_RECEIVE_QUEUE"
   ( "ID" NUMBER DEFAULT "${schema}"."RENDICONTAZIONE_SFTP_RECEIVE_QUEUE_SEQ".NEXTVAL,
	"FILE_NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"STATUS" VARCHAR2(10 CHAR) NOT NULL ENABLE,
	"FILE_SIZE" NUMBER(19,0),
	"SERVER_ID" NUMBER(19,0) NOT NULL ENABLE,
	"HOST_NAME" VARCHAR2(50 CHAR) NOT NULL ENABLE,
	"PORT" NUMBER(19,0) NOT NULL ENABLE,
	"PATH" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"HASH" VARCHAR2(32 CHAR) NOT NULL ENABLE,
	"CONTENT" BLOB NOT NULL ENABLE,
	"SENDER" VARCHAR2(50 CHAR),
	"RECEIVER" VARCHAR2(50 CHAR),
	"INSERTED_TIMESTAMP" TIMESTAMP (6) NOT NULL ENABLE,
	"UPDATED_TIMESTAMP" TIMESTAMP (6) NOT NULL ENABLE,
	"INSERTED_BY" VARCHAR2(35 CHAR) NOT NULL ENABLE,
	"UPDATED_BY" VARCHAR2(35 CHAR) NOT NULL ENABLE
   ) SEGMENT CREATION DEFERRED
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS  LOGGING
  STORAGE(
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "${tablespaceData}"
 LOB ("CONTENT") STORE AS SECUREFILE (
  TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
  NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
  STORAGE(
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
  PARTITION BY RANGE ("INSERTED_TIMESTAMP") INTERVAL (NUMTODSINTERVAL (1, 'DAY'))
 (PARTITION "SYS_P17951"  VALUES LESS THAN (TIMESTAMP' 2019-04-01 00:00:00') SEGMENT CREATION DEFERRED
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "${tablespaceData}"
 LOB ("CONTENT") STORE AS SECUREFILE (
  TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
  NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
  STORAGE(
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))  READ WRITE ) ;

  CREATE TABLE "${schema}"."SCHEDULER_FIRE_CHECK"
     (	"ID" NUMBER DEFAULT "${schema}"."SCHEDULER_FIRE_CHECK_SEQ".NEXTVAL,
  	"JOB_NAME" VARCHAR2(50 CHAR) NOT NULL ENABLE,
  	"EXTRA_KEY" VARCHAR2(500 CHAR) NOT NULL ENABLE,
  	"START" TIMESTAMP (6) NOT NULL ENABLE,
  	"STATUS" VARCHAR2(50 CHAR),
  	"END" TIMESTAMP (6)
     ) SEGMENT CREATION IMMEDIATE
    PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
   NOCOMPRESS LOGGING
    STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
    PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
    BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
    TABLESPACE "${tablespaceData}" ;

      CREATE TABLE "${schema}"."RENDICONTAZIONE"
         (	"ID" NUMBER DEFAULT ${schema}.RENDICONTAZIONE_SEQ.NEXTVAL NOT NULL ENABLE,
      	"OPTLOCK" NUMBER(19,0) NOT NULL ENABLE,
      	"PSP" VARCHAR2(255 CHAR) NOT NULL ENABLE,
      	"INTERMEDIARIO" VARCHAR2(255 CHAR),
      	"CANALE" VARCHAR2(255 CHAR),
      	"PASSWORD" VARCHAR2(255 CHAR),
      	"DOMINIO" VARCHAR2(255 CHAR) NOT NULL ENABLE,
      	"ID_FLUSSO" VARCHAR2(255 CHAR) NOT NULL ENABLE,
      	"DATA_ORA_FLUSSO" TIMESTAMP (6) NOT NULL ENABLE,
      	"FK_BINARY_FILE" NUMBER(19,0),
      	"FK_SFTP_FILE" NUMBER(19,0),
      	"STATO" VARCHAR2(255 CHAR) DEFAULT 'TO_BE_VALIDATED' NOT NULL ENABLE,
      	"INSERTED_TIMESTAMP" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP NOT NULL ENABLE
         ) SEGMENT CREATION IMMEDIATE
        PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
       NOCOMPRESS LOGGING
        STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
        TABLESPACE "${tablespaceData}" ;
      CREATE TABLE "${schema}"."SCHEDULER_TRACE"
         (	"ID" NUMBER DEFAULT "${schema}"."SCHEDULER_TRACE_SEQ".NEXTVAL,
      	"ID_SESSIONE" VARCHAR2(50 CHAR) NOT NULL ENABLE,
      	"JOB_NAME" VARCHAR2(50 CHAR) NOT NULL ENABLE,
      	"START" TIMESTAMP (6) NOT NULL ENABLE,
      	"FIRE" VARCHAR2(50 CHAR) NOT NULL ENABLE,
      	"CRON" VARCHAR2(50 CHAR),
      	"STATUS" VARCHAR2(50 CHAR),
      	"MESSAGE" VARCHAR2(1024 CHAR)
         ) SEGMENT CREATION IMMEDIATE
        PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
       NOCOMPRESS LOGGING
        STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
        TABLESPACE "${tablespaceData}" ;
      CREATE TABLE "${schema}"."BINARY_FILE"
         (	"ID" NUMBER DEFAULT ${schema}.BINARY_FILE_SEQ.NEXTVAL NOT NULL ENABLE,
      	"FILE_SIZE" NUMBER(19,0),
      	"FILE_CONTENT" BLOB,
      	"FILE_HASH" BLOB,
      	"SIGNATURE_TYPE" VARCHAR2(30 CHAR),
      	"XML_FILE_CONTENT" CLOB
         ) SEGMENT CREATION IMMEDIATE
        PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
       NOCOMPRESS LOGGING
        STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
        TABLESPACE "${tablespaceData}"
       LOB ("FILE_CONTENT") STORE AS SECUREFILE (
        TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
        NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
        STORAGE(INITIAL 106496 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
       LOB ("FILE_HASH") STORE AS SECUREFILE (
        TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
        NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
        STORAGE(INITIAL 106496 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
       LOB ("XML_FILE_CONTENT") STORE AS SECUREFILE (
        TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
        NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
        STORAGE(INITIAL 106496 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
        PCTINCREASE 0
        BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;

        CREATE TABLE "${schema}"."RENDICONTAZIONE_SFTP_SEND_QUEUE"
           (	"ID" NUMBER DEFAULT "${schema}"."RENDICONTAZIONE_SFTP_SEND_QUEUE_SEQ".NEXTVAL,
        	"FILE_NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
        	"STATUS" VARCHAR2(10 CHAR) NOT NULL ENABLE,
        	"FILE_SIZE" NUMBER(19,0),
        	"SERVER_ID" NUMBER(19,0) NOT NULL ENABLE,
        	"HOST_NAME" VARCHAR2(50 CHAR) NOT NULL ENABLE,
        	"PORT" NUMBER(19,0) NOT NULL ENABLE,
        	"PATH" VARCHAR2(255 CHAR) NOT NULL ENABLE,
        	"HASH" VARCHAR2(32 CHAR) NOT NULL ENABLE,
        	"CONTENT" BLOB NOT NULL ENABLE,
        	"SENDER" VARCHAR2(50 CHAR),
        	"RECEIVER" VARCHAR2(50 CHAR),
        	"INSERTED_TIMESTAMP" TIMESTAMP (6) NOT NULL ENABLE,
        	"UPDATED_TIMESTAMP" TIMESTAMP (6) NOT NULL ENABLE,
        	"INSERTED_BY" VARCHAR2(35 CHAR) NOT NULL ENABLE,
        	"UPDATED_BY" VARCHAR2(35 CHAR) NOT NULL ENABLE,
        	"RETRY" NUMBER(*,0)
           ) SEGMENT CREATION IMMEDIATE
          PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
         NOCOMPRESS  LOGGING
          STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
          PCTINCREASE 0
          BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
          TABLESPACE "${tablespaceData}"
         LOB ("CONTENT") STORE AS SECUREFILE (
          TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
          NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
          STORAGE(INITIAL 106496 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
          PCTINCREASE 0
          BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
          PARTITION BY RANGE ("INSERTED_TIMESTAMP") INTERVAL (NUMTODSINTERVAL (1, 'DAY'))
         (PARTITION "SYS_P17954"  VALUES LESS THAN (TIMESTAMP' 2019-04-01 00:00:00') SEGMENT CREATION IMMEDIATE
          PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
         NOCOMPRESS LOGGING
          STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
          PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
          BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
          TABLESPACE "${tablespaceData}"
         LOB ("CONTENT") STORE AS SECUREFILE (
          TABLESPACE "${tablespaceLob}" ENABLE STORAGE IN ROW CHUNK 8192
          NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES
          STORAGE(INITIAL 106496 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
          PCTINCREASE 0
          BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))  READ WRITE) ;