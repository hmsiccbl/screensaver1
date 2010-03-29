/* Reports the usage count and volume usage statistics for library
screening plates.  Uses Library Screening Activities (with associated
Plates Used data) to calculate the usage data.  Note that this report
is an interim solution to determining consumed/remaining volume of
library screening plates, since Library Screening Activities do not
yet generate the Well Volume Adjustment records that would otherwise
make this data accessible via the "Well Volumes Browser" in the web
application. */

select l.short_name as library, 
pu.copy_name, 
p.plate_number as plate_number, 
count(*) as usage_count, 
ci.well_volume as initial_volume_nL, 
sum(volume_transferred_per_well * number_of_replicates) as volume_used_nL, 
ci.well_volume - sum(volume_transferred_per_well * number_of_replicates) as volume_remaining_nL  
from plates_used pu 
join (select distinct plate_number from well) as p on(p.plate_number between pu.start_plate and pu.end_plate) 
join library l on(p.plate_number between l.start_plate and l.end_plate) 
join lab_activity la on(la.activity_id = pu.library_screening_id) 
join screening using(activity_id) 
left join copy_info ci on(ci.copy_id = l.short_name || ':' || pu.copy_name and ci.plate_number=p.plate_number)
group by l.short_name, pu.copy_name, p.plate_number, ci.well_volume;
