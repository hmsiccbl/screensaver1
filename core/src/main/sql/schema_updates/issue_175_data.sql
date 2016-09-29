/**
current:

 B-I RNAi 2011
 B-I RNAi 2012
 B-I RNAi 2013
 Clardy Grants
 D'Andrea CMCR
 D'Andrea OHSU
 DARPA 1KM
 Gates-Elledge (R)
 Gray Library Screen
 Harvard Catalyst
 HHMI-Elledge (SM)
 HiTS
 HMS-CETR
 ICCB-L External
 ICCB-L HMS Affiliate
 ICCB-L HMS Quad (Internal)
 ICG
 Levy ADP (2014-2015)
 LINCS
 LSP
 Ludwig
 Marcus Library Screen
 miRNA Consortium 2009
 Mitchison P01
 NERCE/NSRB
 NERCE/NSRB to ICCBL
 Novartis
 NSRB-RNAi External
 NSRB-RNAi HMS Affiliate
 NSRB-RNAi Quad (internal)
 Other
 Sanofi-Aventis
 Unspecified
 Yuan NIH 06-07

Desired:

Harvard Catalyst
External Industry
External Academic
HMS Affiliate
LINCS
LSP
Ludwig
HMS Quad (internal)
Off-site
Other
**/

begin;

update funding_support set is_retired = true;

update funding_support set is_retired = false where value = 'Harvard Catalyst';
update funding_support set is_retired = false where value = 'LINCS';
update funding_support set is_retired = false where value = 'LSP';
update funding_support set is_retired = false where value = 'Ludwig';
update funding_support set is_retired = false where value = 'Other';

insert into funding_support (funding_support_id, value, is_retired) 
	values( nextval('funding_support_id_seq'), 'External Industry', false);
insert into funding_support (funding_support_id, value, is_retired) 
	values( nextval('funding_support_id_seq'), 'External Academic', false);
insert into funding_support (funding_support_id, value, is_retired) 
	values( nextval('funding_support_id_seq'), 'HMS Affiliate', false);
insert into funding_support (funding_support_id, value, is_retired) 
	values( nextval('funding_support_id_seq'), 'HMS Quad (internal)', false);
insert into funding_support (funding_support_id, value, is_retired) 
	values( nextval('funding_support_id_seq'), 'Off-site', false);

commit;

