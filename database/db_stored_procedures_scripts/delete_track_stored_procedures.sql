/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
use caution_lights;
delimiter &
/*------------------------------------------------*/


DROP PROCEDURE IF EXISTS caution_lights.delete_track_configuration;
CREATE PROCEDURE caution_lights.delete_track_configuration(
               IN `track_id` int(10)
              )
BEGIN
   DELETE FROM caution_lights.track_configuration
 	  WHERE row_id=`track_id`;
END;

DROP PROCEDURE IF EXISTS caution_lights.delete_images;
CREATE PROCEDURE caution_lights.delete_images(
               IN `track_id` int(10)
              )
BEGIN
   DELETE FROM caution_lights.images
 	  WHERE track_configuration=`track_id`;
END;

DROP PROCEDURE IF EXISTS caution_lights.delete_node_locations;
CREATE PROCEDURE caution_lights.delete_node_locations(
               IN `track_id` int(10)
              )
BEGIN
   DELETE FROM caution_lights.node_locations
 	  WHERE track_configuration=`track_id`;
END;

DROP PROCEDURE IF EXISTS caution_lights.delete_track;
CREATE PROCEDURE caution_lights.delete_track(
               IN `track_id` int(10) 
              )
BEGIN
  call delete_track_configuration(`track_id`);
  call delete_images(`track_id`);
  call delete_node_locations(`track_id`);
END;
/*--------------------------------------------------------------------------*/


/*------------------------------------------------*/
/*Overhead to facilitate loading from command line*/
&
delimiter ;
/*------------------------------------------------*/

