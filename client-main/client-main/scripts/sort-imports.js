#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

// Порядок групп импортов (в порядке приоритета)
const IMPORT_ORDER = [
  'react',
  '@sber-sbertransport',
  'external', // другие библиотеки (не из списка выше и не локальные)
  'api',
  'constants',
  'hooks',
  'context',
  'utils',
  'i18n',
  'shared',
  'ioc',
  'mf',
  'types',
  'ui',
  'modules',
  'components', // добавлено
  'local',    // относительные импорты ./ и ../
  'assets',
  'styles',
  'styles-module', // *.module.scss и *.module.css
];

/**
 * Определяет группу импорта по его пути
 */
function getImportGroup(importPath) {
  if (importPath === 'react') return 'react';
  if (importPath.startsWith('@sber-sbertransport')) return '@sber-sbertransport';
  if (importPath.startsWith('api/') || importPath === 'api') return 'api';
  if (importPath.startsWith('constants/') || importPath === 'constants') return 'constants';
  if (importPath.startsWith('hooks/') || importPath === 'hooks') return 'hooks';
  if (importPath.startsWith('context/') || importPath === 'context') return 'context';
  if (importPath.startsWith('utils/') || importPath === 'utils') return 'utils';
  if (importPath.startsWith('i18n/') || importPath === 'i18n') return 'i18n';
  if (importPath.startsWith('shared/') || importPath === 'shared') return 'shared';
  if (importPath.startsWith('ioc/') || importPath === 'ioc') return 'ioc';
  if (importPath.startsWith('mf/') || importPath === 'mf') return 'mf';
  if (importPath.startsWith('types/') || importPath === 'types') return 'types';
  if (importPath.startsWith('ui/') || importPath === 'ui') return 'ui';
  if (importPath.startsWith('modules/') || importPath === 'modules') return 'modules';
  if (importPath.startsWith('components/') || importPath === 'components') return 'components';
  if (importPath.startsWith('assets/') || importPath === 'assets') return 'assets';
  if (importPath.startsWith('styles/') || importPath === 'styles') return 'styles';
  if (importPath.endsWith('.module.scss') || importPath.endsWith('.module.css')) return 'styles-module';
  
  // Локальные импорты
  if (importPath.startsWith('./') || importPath.startsWith('../')) return 'local';
  
  // Все остальные - внешние библиотеки
  return 'external';
}

/**
 * Сортирует импорты в группе по алфавиту (без учета регистра)
 */
function sortImportsInGroup(imports) {
  return imports.sort((a, b) => {
    const pathA = a.path.toLowerCase();
    const pathB = b.path.toLowerCase();
    if (pathA < pathB) return -1;
    if (pathA > pathB) return 1;
    return 0;
  });
}

/**
 * Разбирает строку импорта на составляющие
 */
function parseImportLine(line, index) {
  // Импорт со звездочкой: import * as React from 'react';
  const starMatch = line.match(/import\s+\*\s+as\s+(\w+)\s+from\s+['"]([^'"]+)['"];?/);
  if (starMatch) {
    return {
      original: line,
      path: starMatch[2],
      specifier: starMatch[1],
      group: getImportGroup(starMatch[2]),
      index,
      type: 'star',
    };
  }

  // Импорт по умолчанию: import React from 'react';
  const defaultMatch = line.match(/import\s+(\w+)\s+from\s+['"]([^'"]+)['"];?/);
  if (defaultMatch) {
    return {
      original: line,
      path: defaultMatch[2],
      specifier: defaultMatch[1],
      group: getImportGroup(defaultMatch[2]),
      index,
      type: 'default',
    };
  }

  // Деструктурированный импорт: import { useState, useEffect } from 'react';
  const namedMatch = line.match(/import\s+{([^}]+)}\s+from\s+['"]([^'"]+)['"];?/);
  if (namedMatch) {
    return {
      original: line,
      path: namedMatch[2],
      specifier: namedMatch[1].trim(),
      group: getImportGroup(namedMatch[2]),
      index,
      type: 'named',
    };
  }

  // side-effect import: import 'react-dom';
  const sideEffectMatch = line.match(/import\s+['"]([^'"]+)['"];?/);
  if (sideEffectMatch) {
    return {
      original: line,
      path: sideEffectMatch[1],
      specifier: '',
      group: getImportGroup(sideEffectMatch[1]),
      index,
      type: 'side-effect',
    };
  }

  // Импорт с именем и деструктуризацией: import React, { useState } from 'react';
  const mixedMatch = line.match(/import\s+(\w+)\s*,\s*{([^}]+)}\s+from\s+['"]([^'"]+)['"];?/);
  if (mixedMatch) {
    return {
      original: line,
      path: mixedMatch[3],
      specifier: `${mixedMatch[1]}, { ${mixedMatch[2].trim()} }`,
      group: getImportGroup(mixedMatch[3]),
      index,
      type: 'mixed',
    };
  }

  // Не распознанная строка импорта
  return null;
}

/**
 * Собирает строку импорта из составляющих
 */
function buildImportLine(parsed) {
  switch (parsed.type) {
    case 'star':
      return `import * as ${parsed.specifier} from '${parsed.path}';`;
    case 'default':
      return `import ${parsed.specifier} from '${parsed.path}';`;
    case 'named':
      return `import { ${parsed.specifier} } from '${parsed.path}';`;
    case 'side-effect':
      return `import '${parsed.path}';`;
    case 'mixed':
      return `import ${parsed.specifier} from '${parsed.path}';`;
    default:
      return parsed.original;
  }
}

/**
 * Разбирает файл на группы импортов и остальной код
 */
function parseFile(content) {
  const lines = content.split('\n');
  const result = {
    beforeImports: [],
    importGroups: {}, // group -> [parsed imports]
    afterImports: [],
    currentGroup: null,
  };

  // Инициализируем пустые массивы для каждой группы
  IMPORT_ORDER.forEach(group => {
    result.importGroups[group] = [];
  });

  let inImportBlock = false;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const trimmedLine = line.trim();

    // Пропускаем пустые строки и комментарии
    if (!trimmedLine || trimmedLine.startsWith('//') || trimmedLine.startsWith('/*')) {
      if (!inImportBlock) {
        result.beforeImports.push(line);
      } else {
        result.afterImports.push(line);
      }
      continue;
    }

    // Проверяем, является ли строка импортом
    const parsed = parseImportLine(line, i);
    
    if (parsed) {
      inImportBlock = true;
      result.importGroups[parsed.group].push(parsed);
    } else if (inImportBlock && trimmedLine !== '') {
      // Нашли первый не-импорт после блока импортов
      result.afterImports.push(line);
    } else if (!inImportBlock) {
      result.beforeImports.push(line);
    }
  }

  return result;
}

/**
 * Собирает файл из разобранной структуры
 */
function buildFile(parsed, content) {
  const lines = [];

  // Добавляем всё до импортов
  lines.push(...parsed.beforeImports);

  let firstGroup = true;
  let hasImports = false;

  // Добавляем импорты по группам в заданном порядке
  for (const group of IMPORT_ORDER) {
    const imports = parsed.importGroups[group];

    if (imports.length === 0) continue;

    hasImports = true;

    // Добавляем пустую строку между группами
    if (!firstGroup) {
      lines.push('');
    }
    firstGroup = false;

    // Сортируем импорты внутри группы
    const sortedImports = sortImportsInGroup([...imports]);

    // Добавляем импорты
    for (const imp of sortedImports) {
      lines.push(buildImportLine(imp));
    }
  }

  // Добавляем одну пустую строку после импортов (если есть импорты)
  if (hasImports) {
    lines.push('');
  }

  // Фильтруем afterImports: удаляем ведущие пустые строки, оставляем остальной код
  let startIdx = 0;
  for (let i = 0; i < parsed.afterImports.length; i++) {
    if (parsed.afterImports[i].trim() !== '') {
      startIdx = i;
      break;
    }
  }

  // Добавляем всё после импортов
  lines.push(...parsed.afterImports.slice(startIdx));

  // Восстанавливаем завершающий перевод строки, если был
  // Удаляем лишнюю пустую строку в конце, если она есть
  while (lines.length > 0 && lines[lines.length - 1].trim() === '') {
    lines.pop();
  }

  if (content.endsWith('\n')) {
    lines.push('');
  }

  return lines.join('\n');
}

/**
 * Обрабатывает один файл
 */
function processFile(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf-8');
    const parsed = parseFile(content);
    const result = buildFile(parsed, content);
    
    // Проверяем, изменился ли файл
    if (result !== content) {
      fs.writeFileSync(filePath, result, 'utf-8');
      console.log(`✓ Отсортировано: ${filePath}`);
      return true;
    } else {
      console.log(`- Без изменений: ${filePath}`);
      return false;
    }
  } catch (error) {
    console.error(`✗ Ошибка обработки ${filePath}:`, error.message);
    return false;
  }
}

/**
 * Рекурсивно находит все ts и tsx файлы в директории
 */
function findTsFiles(dir, files = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    
    if (entry.isDirectory()) {
      // Пропускаем node_modules и скрытые директории
      if (entry.name === 'node_modules' || entry.name.startsWith('.')) {
        continue;
      }
      findTsFiles(fullPath, files);
    } else if (entry.isFile() && (entry.name.endsWith('.ts') || entry.name.endsWith('.tsx'))) {
      files.push(fullPath);
    }
  }
  
  return files;
}

/**
 * Главная функция
 */
function main() {
  const args = process.argv.slice(2);
  
  if (args.includes('--help') || args.includes('-h')) {
    console.log(`
Скрипт для сортировки импортов в TypeScript файлах

Использование:
  node scripts/sort-imports.js [путь]

Аргументы:
  [путь]  Путь к файлу или директории (по умолчанию: текущая директория)

Группы импортов (в порядке сортировки):
  1. react
  2. @sber-sbertransport
  3. другие библиотеки (external)
  4. api, constants, hooks, context, utils, i18n, shared, ioc, mf, types, ui, modules, components
  5. локальные импорты (./, ../)
  6. assets
  7. styles
  8. *.module.scss и *.module.css

Примеры:
  node scripts/sort-imports.js                    # обработать все файлы в проекте
  node scripts/sort-imports.js src/components     # обработать файлы в директории
  node scripts/sort-imports.js src/App.tsx        # обработать один файл
`);
    process.exit(0);
  }

  let targetPath = args[0] || process.cwd();
  targetPath = path.resolve(targetPath);

  if (!fs.existsSync(targetPath)) {
    console.error(`Ошибка: путь не существует: ${targetPath}`);
    process.exit(1);
  }

  let files = [];
  if (fs.statSync(targetPath).isDirectory()) {
    files = findTsFiles(targetPath);
  } else if (targetPath.endsWith('.ts') || targetPath.endsWith('.tsx')) {
    files = [targetPath];
  } else {
    console.error(`Ошибка: поддерживаются только .ts и .tsx файлы: ${targetPath}`);
    process.exit(1);
  }

  console.log(`Найдено файлов: ${files.length}\n`);

  let processed = 0;
  let modified = 0;

  for (const filePath of files) {
    processed++;
    if (processFile(filePath)) {
      modified++;
    }
  }

  console.log(`\n${'='.repeat(50)}`);
  console.log(`Обработано файлов: ${processed}`);
  console.log(`Изменено: ${modified}`);
  console.log(`Без изменений: ${processed - modified}`);
}

main();
