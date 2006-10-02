begin;

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'Hydrobromide', 't', 'Br');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'bromide');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'Hydrobromide', 't', '[Br-]');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'bromide');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'dibromide', 't', 'BrBr');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'iodine bromide', 't', 'BrI');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tribromide', 't', 'Br[BrH]Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Br[Cu]Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Br[Fe](Br)(Br)Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Br[IH]I');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Br[Mg]Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Br[Zn](Br)Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'carbon', 't', 'C');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'C1CNCCN1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'ethylene', 't', 'C=C');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'C=COC=C');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'carbon monoxide', 't', 'C=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CC');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'diaceturate', 't', 'CC(=O)NCC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'acetate', 't', 'CC(=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'acetate', 't', 'CC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfopropanoic acid', 't', 'CC(C(=O)O)S(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'isopropyl alcohol', 't', 'CC(C)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'lactate', 't', 'CC(O)C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CCC');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CCCCCCCCCCCCCCCCCC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CCCCCCCCCCCCOS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'acetonitrile', 't', 'CCN');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'triethyl ammonium', 't', 'CCN(CC)CC');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CCO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'ethyl sulfate', 't', 'CCOS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'diethanesulfonate', 't', 'CCS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfonate', 't', 'O=S(=O)=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'triethylamine', 't', 'CC[NH+](CC)CC');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methiodide', 't', 'CI');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CN');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'dimethylamine', 't', 'CNC');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methoxide', 't', 'CO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'COC=C');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methyl sulfate', 't', 'COS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methyl sulfate', 't', 'COS(=O)(=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methyl nitrate', 't', 'CO[N+](=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methanosulfate', 't', 'CS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'methanosulfate', 't', 'CS(=O)(=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CS(=O)(=O)c1ccccc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'CSC');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'C[Au](C)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetramethylammonium', 't', 'C[N+](C)(C)C');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'C[N+]1(CCCCC[N+]2(C)CCCC2)CCCC1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tosylate', 't', 'Cc1ccc(cc1)S(=O)(=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tosylate', 't', 'Cc1ccc(cc1)S(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'ClC(Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'ClC(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'ClC=C');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Cu](Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Cu]Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Fe](Cl)(Cl)(Br)Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Fe](Cl)(Cl)Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetrachloroferrate', 't', 'Cl[Fe](Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hydrogen dichloride', 't', 'Cl[H]Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Hg]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Hg](Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Hg]Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[IH]Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Pd](Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Pt](Cl)(Cl)(Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[Cl-][Pt]([Cl-])([Cl-])([Cl-])([Cl-])[Cl-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'trichlorocarbonylplatinate', 't', 'Cl[Pt](Cl)(Cl)C#[O+]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tertachloroplatinate', 't', 'Cl[Pt](Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'antimony(III) chloride', 't', 'Cl[Sb](Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetrachlorozincate', 't', 'Cl[Zn](Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetrachlorozincate', 't', '[Cl-][Zn+2]([Cl-])[Cl-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Zn](Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'zinc chloride', 't', 'Cl[Zn]Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetrafluoroborate', 't', 'FB(F)(F)F');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'boron tetrafluoride');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetrafluoroborate', 't', '[F-][B+3]([F-])([F-])[F-]');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'boron tetrafluoride');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tetrafluoroborate', 't', 'F[B-](F)(F)F');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'boron tetrafluoride');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexafluorophosphate', 't', 'FP(F)(F)(F)(F)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexafluorophosphate', 't', '[F-][P+5]([F-])([F-])([F-])([F-])[F-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexafluoroarsenate', 't', 'F[As](F)(F)(F)(F)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexafluoroarsenate', 't', '[F-][As+5]([F-])([F-])([F-])([F-])[F-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'F[H]F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'F[Sb](F)(F)(F)(F)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexafluorosilicate', 't', 'F[Si](F)(F)(F)(F)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexafluorosilicate', 't', '[F-][Si]([F-])([F-])([F-])([F-])[F-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'Hydrochloride', 't', 'HCl');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'HCl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'Hydrochloride', 't', '[Hcl-]');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'HCl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'Hydrochloride', 't', 'Cl');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'HCl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'Hydrochloride', 't', '[Cl-]');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'HCl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'iodide', 't', 'I');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'iodide', 't', '[I-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'diiodide', 't', 'II');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'triiodide', 't', 'I[IH]I');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'potassium', 't', 'K');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'potassium', 't', '[K+]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'potassium', 't', '[KH]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'potassium', 't', '[K]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'N');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'N=[N+]=[N-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[N-]=[N+]=[N-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'NC(CO)(CO)CO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'cyclohexylamine', 't', 'NC1CCCCC1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'ethylenediamine', 't', 'NCCN');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'EDA');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'NCCO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'NN');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'NO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sodium', 't', 'Na');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sodium', 't', '[Na+]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sodium', 't', '[NaH]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sodium', 't', '[Na]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'O=C=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OC(=O)/C=C/C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OC(=O)/C=C\C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'oxalate', 't', 'OC(=O)C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'trifluoroacetate', 't', 'OC(=O)C(F)(F)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'maleate', 't', 'OC(=O)C=CC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'citrate', 't', 'OC(=O)CC(O)(CC(=O)O)C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OC(=O)CCC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'chloroacetic acid', 't', 'OC(=O)CCl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'phosphonoacetic acid', 't', 'OC(=O)CP(=O)(O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfuric acid', 't', 'OC(=O)CS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfosalicylate', 't', 'OC(=O)c1cc(ccc1O)S(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'benzoate', 't', 'OC(=O)c1ccccc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'salicylate', 't', 'OC(=O)c1ccccc1O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tartrate', 't', 'OC(C(O)C(=O)O)C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'tartrate', 't', 'OC(C(O)C(=O)[O-])C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'formic acid', 't', 'OC=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OCC(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OCCO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OCCOS(=O)=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'isethionate', 't', 'OCCS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OCO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'perchlorate', 't', 'OCl(=O)(=O)=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'iodic acid', 't', 'OI(=O)=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'nitrate', 't', 'ON(O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'nitrite', 't', 'ON=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'peroxide', 't', 'OO');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'phosphate', 't', 'OP(=O)([O-])[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'phosphate', 't', 'OP(=O)(O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'phosphonate', 't', 'OP(=O)=O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'trifluoromethanesulfonic acid', 't', 'OS(=O)(=O)C(F)(F)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'edisylate', 't', 'OS(=O)(=O)CCS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'fluorosulfate', 't', 'OS(=O)(=O)F');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfate', 't', 'OS(=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfate', 't', '[O-]S(=O)(=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'sulfate', 't', 'OS(=O)(=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OS(=O)(=O)c1ccccc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'OS(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'O[C@@H]([C@@H](O)C(=O)O)C(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'chromic acid', 't', 'O[Cr](=O)(=O)O');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'nitrate', 't', 'O[N+](=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'picrate', 't', 'Oc1c(cc(cc1N(=O)=O)[N+](=O)[O-])[N+](=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'PP');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'thiocyanate', 't', 'SC#N');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[3H][4H]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[4HH]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'borohydride', 't', '[BH4]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[Br-][Pt+2]([Br-])([Br-])[Br-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'calcium', 't', '[Ca+2]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'calcium', 't', '[CaH2]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[Cl-][Au+3]([Cl-])([Cl-])[Cl-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', 'Cl[Au](Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[Cl-][Pd+2]([Cl-])([Cl-])[Cl-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[Cl-][Pt+2]([Cl-])([Cl-])[Cl-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexachloroantimonate', 't', '[Cl-][Sb+3]([Cl-])([Cl-])([Cl-])([Cl-])[Cl-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'hexachloroantimonate', 't', 'Cl[Sb](Cl)(Cl)(Cl)(Cl)Cl');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[H-89]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[K]Br');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'lithium', 't', '[Li+]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'lithium', 't', '[LiH]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'lithium', 't', '[Li]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'magnesium', 't', '[Mg+2]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'ammonium', 't', '[NH4+]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[O-][N+](=O)[O-]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'selenocyanate', 't', '[Se-]C#N');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, NULL, 't', '[Y+16]');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'benzene', 't', 'c1ccccc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'pyridine', 't', 'c1ccncc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'piperidine', 't', 'C1CCNCC1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'pyrazine', 't', 'n1ccncc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'piperazine', 't', 'N1CCNCC1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'fluorobenzene', 't', 'Fc1ccccc1');

insert into compound (compound_id, version, compound_name, is_salt, smiles) values (nextval('compound_id_seq'), 1, 'anisole', 't', 'COc1ccccc1');
insert into compound_synonym (compound_id, synonym) values (currval('compound_id_seq'), 'methoxybenzene');

commit;
