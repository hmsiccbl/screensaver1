/* Reports the usage count and volume usage statistics for library
screening plates.  Uses Library Screening Activities (with associated
Plates Used data) to calculate the usage data.  Note that this report
is an interim solution to determining consumed/remaining volume of
library screening plates, since Library Screening Activities do not
yet generate the Well Volume Adjustment records that would otherwise
make this data accessible via the "Well Volumes Browser" in the web
application. */

select l.short_name as library, 
c.name, 
p.plate_number as plate_number, 
count(*) as usage_count, 
p.well_volume as initial_volume_nL, 
sum(la.volume_transferred_per_well * s.number_of_replicates) as volume_used_nL, 
p.well_volume - sum(la.volume_transferred_per_well * s.number_of_replicates) as volume_remaining_nL  
from library l 
join copy c using(library_id)
join plate p using(copy_id)
join assay_plate ap using(plate_id)
join lab_activity la on(ap.library_screening_id = la.activity_id)
join screening s using(activity_id)
group by l.short_name, c.name, p.plate_number, p.well_volume;
