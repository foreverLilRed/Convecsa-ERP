UPDATE plantillas_contrato
SET plantilla = regexp_replace(
        plantilla,
        '<div class="print-bar no-print">(.|\n)*?</div>[[:space:]]*',
        '',
        'g'
    )
WHERE uso = 'ADENDA'
  AND plantilla LIKE '%print-bar%';
