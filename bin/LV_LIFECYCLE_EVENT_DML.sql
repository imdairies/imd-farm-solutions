/* This script inserts entries into the Life cycle event lookup. */

select * from imd.LV_LIFECYCLE_EVENT;
delete from imd.LV_LIFECYCLE_EVENT;
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'DEWORM','Y','Deworming','Rid the animal of internal parasites','KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'VACCINE'    ,'Y','Vaccination','Vaccination'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'GROOM'     ,'Y','Grooming' ,'Grooming'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES (  'PATRURATE'     ,'Y','Parturition'  ,'Parturition'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'TICK'     ,'Y','Tick Treatment','Tick Treatment','KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'DRY-OFF'     ,'Y','Dry Off' ,'Dry Off'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'RATION'     ,'Y','Feed Ration' ,'Feed Ration Calculation'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'DEHORN'     ,'Y','Dehorning','Dehorning'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'INSEMINATE'    ,'Y','Insemination','Insemination'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'HEAT'     ,'Y','Heat'  ,'Heat'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'STDNGHEAT'     ,'Y','Standing Heat','Standing Heat'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'TREATMENT' ,'Y','Medical Treatment'  ,'Medical Treatment'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'FEVER'     ,'Y','Fever'  ,'Fever'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'MILKQLTY'     ,'Y','Milk Quality' ,'Milk Quality Measurement'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'PREGTEST' ,'Y','Pregnancy Test' ,'Pregnancy Test'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES ( 'DBLINSEM'     ,'Y','Double Dose Insemination'  ,'Double Dose Insemination'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
INSERT INTO IMD.LV_LIFECYCLE_EVENT VALUES (  'ABORTION'    ,'Y','Abortion'  ,'Abortion or Miscarriage'  ,'KASHIF',SYSDATE(),'KASHIF',SYSDATE());
select * from imd.LV_LIFECYCLE_EVENT;