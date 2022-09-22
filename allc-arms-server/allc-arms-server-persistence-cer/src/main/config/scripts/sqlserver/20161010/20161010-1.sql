ALTER TABLE CO_DSC_EMP ADD MO_DSC_EMP_2	decimal(14,3) NOT NULL DEFAULT 0;
-------------------------------------------------------------------------------
UPDATE CO_DSC_EMP SET MO_DSC_EMP_2 = MO_DSC_EMP;
ALTER TABLE CO_DSC_EMP DROP COLUMN MO_DSC_EMP;
EXEC sp_rename 'CO_DSC_EMP.MO_DSC_EMP_2', 'MO_DSC_EMP', 'COLUMN';
------------------------------------------------------------------------------
ALTER TABLE CO_DSC_EMP_TOT ADD MO_DSC_TOT_2 decimal(14,3) NOT NULL DEFAULT 0;
------------------------------------------------------------------------------
UPDATE CO_DSC_EMP_TOT SET MO_DSC_TOT_2 = MO_DSC_TOT;
ALTER TABLE CO_DSC_EMP_TOT DROP COLUMN MO_DSC_TOT;
EXEC sp_rename 'CO_DSC_EMP_TOT.MO_DSC_TOT_2', 'MO_DSC_TOT', 'COLUMN';