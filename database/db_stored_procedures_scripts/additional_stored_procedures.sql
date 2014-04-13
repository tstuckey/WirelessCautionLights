/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
use caution_lights;
delimiter &
/*------------------------------------------------*/

DROP PROCEDURE IF EXISTS caution_lights.prepare_track_configuration;
CREATE PROCEDURE caution_lights.prepare_track_configuration(
               IN `in_track_configuration` int(10),
               OUT `out_track_configuration` int(10) 
              )
BEGIN
   DECLARE existing_track_configuration int(10); 
   DECLARE no_rows_found INT DEFAULT 0;

   DECLARE track_cursor CURSOR FOR
		SELECT row_id FROM caution_lights.track_configuration
			      WHERE row_id=`in_track_configuration`; 

   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET no_rows_found = 1; /*To handle when no rows found*/

    OPEN track_cursor;
      FETCH track_cursor into existing_track_configuration;
    CLOSE track_cursor; 

    /*if the existing_track_configuration was null,we need to create a track_configuration entry*/
    /*and return the new row_id */
    IF existing_track_configuration<=>NULL THEN

       /*create an track_configuration entry with the new setting*/
       INSERT INTO caution_lights.track_configuration () VALUES(); 
       SET @new_track_configuration=LAST_INSERT_ID();
       SET `out_track_configuration`=@new_track_configuration;
       ELSE
       /*otherwise return the existing_track_configuration*/
        SET `out_track_configuration`=existing_track_configuration;
     END IF;
END;

DROP PROCEDURE IF EXISTS caution_lights.prepare_images;
CREATE PROCEDURE caution_lights.prepare_images(
               IN `in_track_configuration` int(10),
               OUT `out_images_id` int(10) 
              )
BEGIN
   DECLARE existing_image_id int(10); 
   DECLARE no_rows_found INT DEFAULT 0;


   DECLARE images_cursor CURSOR FOR
		SELECT row_id FROM caution_lights.images
			      WHERE track_configuration=`in_track_configuration`; 

   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET no_rows_found = 1; /*To handle when no rows found*/

    OPEN images_cursor;
      FETCH images_cursor into existing_image_id;
    CLOSE images_cursor; 

    /*if the existing_track_configuration was null,we need to create a track_configuration entry*/
    /*and return the new row_id */
    IF existing_image_id<=>NULL THEN
       SET @new_image_id=0; /*some initialization*/

       /*create an track_configuration entry with the new setting*/
       INSERT INTO caution_lights.images () VALUES(); 
       SET @new_image_id=LAST_INSERT_ID();
       SET `out_images_id`=@new_image_id;
       ELSE
       /*otherwise return the existing_track_configuration*/
        SET `out_images_id`=existing_image_id;
     END IF;
END;

DROP PROCEDURE IF EXISTS caution_lights.save_track_pic;
CREATE PROCEDURE caution_lights.save_track_pic(
		IN `in_track_configuration_id` int (10),
		IN `in_description` varchar (100),
		IN `in_track_photo` mediumblob 
       )

BEGIN 
    SET @valid_track_configuration=0;
    SET @valid_images_id=0;

    call prepare_track_configuration(`in_track_configuration_id`,@valid_track_configuration); 
    UPDATE caution_lights.track_configuration SET description=`in_description` WHERE row_id=@valid_track_configuration;

    call prepare_images(@valid_track_configuration,@valid_images_id); 
    UPDATE caution_lights.images SET track_configuration=@valid_track_configuration WHERE row_id=@valid_images_id;
    UPDATE caution_lights.images SET photo=`in_track_photo` WHERE row_id=@valid_images_id;
 
    call delete_node_locations(@valid_track_configuration);/*delete any nodes for this track, this facilitates iterating 
                                                            *through the save_light_node routine for each light_node*/
    SELECT @valid_track_configuration AS 'updated_id'; 
    FLUSH QUERY CACHE;
END;

DROP PROCEDURE IF EXISTS caution_lights.update_track_description;
CREATE PROCEDURE caution_lights.update_track_description(
		IN `in_track_configuration_id` int (10),
		IN `in_description` varchar (100)
       )

BEGIN 
    SET @valid_track_configuration=0;
    call prepare_track_configuration(`in_track_configuration_id`,@valid_track_configuration); 
    UPDATE caution_lights.track_configuration SET description=`in_description` WHERE row_id=@valid_track_configuration;
END;

DROP PROCEDURE IF EXISTS caution_lights.clear_nodes_at_track;
CREATE PROCEDURE caution_lights.clear_nodes_at_track(
		IN `in_track_configuration_id` int (10)
       )

BEGIN 
    DELETE FROM caution_lights.node_locations WHERE track_configuration=`in_track_configuration_id`;
END;

DROP PROCEDURE IF EXISTS caution_lights.save_light_node;
CREATE PROCEDURE caution_lights.save_light_node(
		IN `in_track_configuration_id` int (10),
		IN `in_node_description` varchar (100),
		IN `in_slave_address` varchar (3),
		IN `in_x_coord` int,
		IN `in_y_coord` mediumblob,
		IN `in_keyboard_shortcut` varchar (10)
       )

BEGIN 
    DECLARE new_node_locations_id int(10); 

    INSERT INTO caution_lights.node_locations () VALUES(); 
    SET new_node_locations_id=LAST_INSERT_ID();
        
    UPDATE caution_lights.node_locations SET description=`in_node_description` WHERE row_id=new_node_locations_id;
    UPDATE caution_lights.node_locations SET track_configuration=`in_track_configuration_id` WHERE row_id=new_node_locations_id;
    UPDATE caution_lights.node_locations SET slave_address=`in_slave_address` WHERE row_id=new_node_locations_id;
    UPDATE caution_lights.node_locations SET x_coord=`in_x_coord` WHERE row_id=new_node_locations_id;
    UPDATE caution_lights.node_locations SET y_coord=`in_y_coord` WHERE row_id=new_node_locations_id;
    UPDATE caution_lights.node_locations SET keyboard_shortcut=`in_keyboard_shortcut` WHERE row_id=new_node_locations_id;
END;



DROP PROCEDURE IF EXISTS caution_lights.get_track_info;
CREATE PROCEDURE get_track_info(
		IN `in_track_id` int (10)
                 )
BEGIN
  SELECT 
	TRACK_CFG.description,
	TRACK_IMG.photo
  FROM caution_lights.track_configuration as TRACK_CFG
	LEFT JOIN caution_lights.images as TRACK_IMG
		ON TRACK_CFG.row_id=TRACK_IMG.track_configuration
	WHERE
		TRACK_CFG.row_id=`in_track_id`;

END;

DROP PROCEDURE IF EXISTS caution_lights.get_light_nodes;
CREATE PROCEDURE get_light_nodes(
		IN `in_track_id` int (10)
                 )
BEGIN
  SELECT 
	description,
	slave_address,
	x_coord,
	y_coord,	
	keyboard_shortcut
  FROM caution_lights.node_locations
	WHERE track_configuration=`in_track_id`;

END;

/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
&
delimiter ;
/*------------------------------------------------*/

