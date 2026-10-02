UPDATE plantillas_contrato
SET plantilla = regexp_replace(
        plantilla,
        '<div class="print-bar no-print">(.|\n)*?</div>[[:space:]]*',
        '',
        'g'
    )
WHERE plantilla LIKE '%print-bar%';
