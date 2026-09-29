import { IDepartment } from 'stores/Organizations/Organizations.interface';
import { buildDepartmentTree } from '../buildTreeSelectData';

const dept = (id: string, departmentName: string, parentId: string | null = null): IDepartment => ({
  id: id as IDepartment['id'],
  departmentName,
  parentId: parentId as IDepartment['parentId'],
});

describe('buildDepartmentTree', () => {
  it('возвращает пустой массив для пустого списка', () => {
    expect(buildDepartmentTree([])).toEqual([]);
  });

  it('строит дерево с одним корнем, если передан один департамент', () => {
    const result = buildDepartmentTree([dept('1', 'Корневой')]);

    expect(result).toHaveLength(1);
    expect(result[0].id).toBe('1');
    expect(result[0].title).toBe('Корневой');
    expect(result[0].children).toEqual([]);
  });

  it('строит дерево с двумя независимыми корнями', () => {
    const result = buildDepartmentTree([
      dept('1', 'Корень 1'),
      dept('2', 'Корень 2'),
    ]);

    expect(result).toHaveLength(2);
    expect(result.map(n => n.id)).toEqual(['1', '2']);
    expect(result.every(n => n.children)).toBe(true);
  });

  it('строит двухуровневое дерево', () => {
    const result = buildDepartmentTree([
      dept('1', 'Родитель'),
      dept('2', 'Потомок', '1'),
    ]);

    expect(result).toHaveLength(1);
    expect(result[0].id).toBe('1');
    expect(result[0].children).toHaveLength(1);
    expect(result[0].children?.[0].id).toBe('2');
    expect(result[0].children?.[0].title).toBe('Потомок');
    expect(result[0].children?.[0].children).toEqual([]);
  });

  it('строит трёхуровневое дерево', () => {
    const result = buildDepartmentTree([
      dept('1', 'Дед'),
      dept('2', 'Отец', '1'),
      dept('3', 'Сын', '2'),
    ]);

    expect(result).toHaveLength(1);
    const root = result[0];
    expect(root.id).toBe('1');
    expect(root.children).toHaveLength(1);

    const middle = root.children![0];
    expect(middle.id).toBe('2');
    expect(middle.children).toHaveLength(1);

    const leaf = middle.children![0];
    expect(leaf.id).toBe('3');
    expect(leaf.children).toEqual([]);
  });

  it('корректно формирует поля узла: key, id и value равны, title равен departmentName', () => {
    const result = buildDepartmentTree([dept('1', 'Департамент')]);

    expect(result[0].key).toBe('1');
    expect(result[0].id).toBe('1');
    expect(result[0].value).toBe('1');
    expect(result[0].title).toBe('Департамент');
  });

  it('считает узел с отсутствующим в списке parentId корневым', () => {
    const result = buildDepartmentTree([
      dept('1', 'Сирота-1', 'missing-parent'),
      dept('2', 'Сирота-2', 'another-missing'),
    ]);

    // Условие: parentId===null && !departmentList.some(d => d.id === item.parentId).
    // Для сирот в списке нет элемента с их parentId, поэтому они считаются корнями.
    expect(result).toHaveLength(2);
    expect(result.map(n => n.id).sort()).toEqual(['1', '2']);
  });

  it('смешанное дерево: несколько корней с потомками', () => {
    const result = buildDepartmentTree([
      dept('1', 'A'),
      dept('2', 'B'),
      dept('3', 'A-1', '1'),
      dept('4', 'A-2', '1'),
      dept('5', 'B-1', '2'),
    ]);

    expect(result).toHaveLength(2);

    const a = result.find(n => n.id === '1')!;
    const b = result.find(n => n.id === '2')!;

    expect(a.children!.map(c => c.id).sort()).toEqual(['3', '4']);
    expect(b.children!.map(c => c.id)).toEqual(['5']);
    expect(b.children![0].children).toEqual([]);
  });
});
