/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
use caution_lights;
delimiter &
/*------------------------------------------------*/


DROP PROCEDURE IF EXISTS caution_lights.clone_track;
CREATE PROCEDURE caution_lights.clone_track(
               IN `old_track_id` int(10)
              )

BEGIN
    INSERT INTO caution_lights.track_configuration
                          (description) 
                    SELECT description 
                    FROM caution_lights.track_configuration 
	            WHERE row_id=`old_track_id`;

    SET @new_track_id=LAST_INSERT_ID();

    call clone_images(`old_track_id`,@new_track_id);
    call clone_node_locations(`old_track_id`,@new_track_id);

  SELECT @new_track_id AS "new_track_id"; /*send the new id back to the front end*/
END;

DROP PROCEDURE IF EXISTS caution_lights.clone_images;
CREATE PROCEDURE caution_lights.clone_images(
               IN `old_track_id` int(10),
               IN `new_track_id` int(10)
              )
BEGIN
   /*insert a clone of the inputed setting record*/
   INSERT INTO caution_lights.images (track_configuration, photo) 
                    SELECT `new_track_id`, photo 
                    FROM caution_lights.images
	            WHERE track_configuration=`old_track_id`;
END;

DROP PROCEDURE IF EXISTS caution_lights.clone_node_locations;
CREATE PROCEDURE caution_lights.clone_node_locations(
               IN `old_track_id` int(10),
               IN `new_track_id` int(10)
              )
BEGIN
   /*insert a clone of the inputed setting record*/
   INSERT INTO caution_lights.node_locations (description,track_configuration,slave_address,
                                              x_coord,y_coord,keyboard_shortcut) 
                    SELECT description, `new_track_id`, slave_address,
	                   x_coord, y_coord,keyboard_shortcut
                    FROM caution_lights.node_locations
	            WHERE track_configuration=`old_track_id`;
END;

/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
&
delimiter ;
/*------------------------------------------------*/

