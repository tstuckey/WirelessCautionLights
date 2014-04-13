/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
use caution_lights;
delimiter &
/*------------------------------------------------*/


DROP PROCEDURE IF EXISTS caution_lights.get_number_of_tracks;
CREATE PROCEDURE caution_lights.get_number_of_tracks (
       )

BEGIN
   DECLARE done INT DEFAULT 0;
   DECLARE this_track int(10); 

   DECLARE track_cursor CURSOR FOR SELECT row_id FROM caution_lights.track; 
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1; /*To handle when no rows found*/
   SET @records_per_page=50;

   SELECT count(row_id) AS 'total', @records_per_page AS 'records per page' 
           FROM caution_lights.track_configuration; 
END;


DROP PROCEDURE IF EXISTS caution_lights.prepare_limit_statement;
CREATE PROCEDURE caution_lights.prepare_limit_statement(
               IN `in_page` int(3),
               OUT `out_limit_statement` varchar(50) 
              )
BEGIN
  /*we are going to return 100 records per page*/
  SET @records_per_page=50;
  /*take whatever page of results we want to retrieve*/
  /*multiply it by the number of results we are displaying per page*/
  /*and subtract the number of results we are displaying per page*/
  SET @start_value=`in_page` * @records_per_page - @records_per_page;
  SET `out_limit_statement`=CONCAT(" LIMIT ",@start_value,",",@records_per_page); 
END;

DROP PROCEDURE IF EXISTS caution_lights.find_tracks;
CREATE PROCEDURE caution_lights.find_tracks (
               IN `in_page` int(3)
       )

BEGIN
   DECLARE done INT DEFAULT 0;
   DECLARE this_track int(10); 

   DECLARE track_cursor CURSOR FOR SELECT row_id FROM caution_lights.track; 
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1; /*To handle when no rows found*/

   SET @start_syntax="
       SELECT 
       	      row_id AS 'Identifier', 
	      description AS 'Description'
       FROM caution_lights.track_configuration "; 


     SET @order_by_syntax=CONCAT(" ORDER BY Identifier  "); 
     call prepare_limit_statement(`in_page`,@limit_syntax);

     SET @syntax=CONCAT(@start_syntax, @order_by_syntax,@limit_syntax,";");

     PREPARE stmt1 FROM @syntax;
     EXECUTE stmt1;
END;


/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
&
delimiter ;
/*------------------------------------------------*/

