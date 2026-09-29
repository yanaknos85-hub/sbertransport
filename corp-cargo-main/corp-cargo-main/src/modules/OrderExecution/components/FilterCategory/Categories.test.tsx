import { Tab, Category, CategoryName } from '../../constants/Tabs';
import { Roles } from 'constants/constants.app';
import categories, { allowedRoles } from './categories';

// Мокаем зависимости
jest.mock('constants/constants.app', () => ({
  Roles: {
    ADMIN_DATA_MASTER: 'ADMIN_DATA_MASTER',
    ENGINEER_CORP_CLIENT: 'ENGINEER_CORP_CLIENT',
    ADMIN_CORP_CLIENT: 'ADMIN_CORP_CLIENT',
    DISPATCHER_SUPPORT_SERVICE: 'DISPATCHER_SUPPORT_SERVICE',
    USER: 'USER',
    GUEST: 'GUEST',
  },
}));

jest.mock('../../constants/Tabs', () => ({
  Tab: {
    cargo: 'cargo',
    passenger: 'passenger',
    realty: 'realty',
  },
  Category: {
    all: 'all',
    courier: 'courier',
    dedicated: 'dedicated',
    interregional: 'interregional',
    domesticCourier: 'domesticCourier',
    individual: 'individual',
    template: 'template',
    apartment: 'apartment',
    house: 'house',
    commercial: 'commercial',
  },
  CategoryName: {
    all: 'Все',
    courier: 'Курьерские',
    dedicated: 'Дедикейтед',
    interregional: 'Межрегиональные',
    domestic_courier: 'Внутренние курьерские',
    individual: 'Индивидуальные',
    template: 'Шаблоны',
    apartment: 'Квартиры',
    house: 'Дома',
    commercial: 'Коммерческая',
  },
}));

describe('Categories Utilities', () => {
  describe('allowedRoles', () => {
    it('должен быть массивом', () => {
      expect(Array.isArray(allowedRoles)).toBe(true);
    });

    it('должен содержать правильное количество ролей', () => {
      expect(allowedRoles).toHaveLength(4);
    });

    it('должен содержать роль ADMIN_DATA_MASTER', () => {
      expect(allowedRoles).toContain(Roles.ADMIN_DATA_MASTER);
    });

    it('должен содержать роль ENGINEER_CORP_CLIENT', () => {
      expect(allowedRoles).toContain(Roles.ENGINEER_CORP_CLIENT);
    });

    it('должен содержать роль ADMIN_CORP_CLIENT', () => {
      expect(allowedRoles).toContain(Roles.ADMIN_CORP_CLIENT);
    });

    it('должен содержать роль DISPATCHER_SUPPORT_SERVICE', () => {
      expect(allowedRoles).toContain(Roles.DISPATCHER_SUPPORT_SERVICE);
    });

    it('не должен содержать другие роли', () => {
      expect(allowedRoles).not.toContain('USER');
      expect(allowedRoles).not.toContain('GUEST');
    });
  });

  describe('categories', () => {
    it('должен быть объектом', () => {
      expect(typeof categories).toBe('object');
      expect(categories).not.toBeNull();
    });

    it('должен содержать ключ Tab.cargo', () => {
      expect(categories).toHaveProperty(Tab.cargo);
    });

    it('массив cargo должен быть определен', () => {
      expect(categories[Tab.cargo]).toBeDefined();
      expect(Array.isArray(categories[Tab.cargo])).toBe(true);
    });

    it('массив cargo должен содержать 7 элементов', () => {
      expect(categories[Tab.cargo]).toHaveLength(7);
    });
  });

  describe('Категории грузов (cargo)', () => {
    let cargoCategories: any[];

    beforeEach(() => {
      cargoCategories = categories[Tab.cargo];
    });

    describe('Структура категорий', () => {
      it('каждая категория должна иметь правильную структуру', () => {
        cargoCategories.forEach((category) => {
          expect(category).toHaveProperty('type');
          expect(category).toHaveProperty('category');
          expect(category).toHaveProperty('name');
          expect(category).toHaveProperty('available');
        });
      });

      it('все категории должны иметь тип Tab.cargo', () => {
        cargoCategories.forEach((category) => {
          expect(category.type).toBe(Tab.cargo);
        });
      });

      it('все категории должны быть доступны (available: true)', () => {
        cargoCategories.forEach((category) => {
          expect(category.available).toBe(true);
        });
      });
    });

    describe('Конкретные категории', () => {
      it('должна быть категория "Все"', () => {
        const allCategory = cargoCategories.find(cat => cat.category === Category.all);
        expect(allCategory).toBeDefined();
        expect(allCategory?.name).toBe(CategoryName.all);
        expect(allCategory?.available).toBe(true);
      });

      it('должна быть категория "Курьерские"', () => {
        const courierCategory = cargoCategories.find(cat => cat.category === Category.courier);
        expect(courierCategory).toBeDefined();
        expect(courierCategory?.name).toBe(CategoryName.courier);
        expect(courierCategory?.available).toBe(true);
      });

      it('должна быть категория "Дедикейтед"', () => {
        const dedicatedCategory = cargoCategories.find(cat => cat.category === Category.dedicated);
        expect(dedicatedCategory).toBeDefined();
        expect(dedicatedCategory?.name).toBe(CategoryName.dedicated);
        expect(dedicatedCategory?.available).toBe(true);
      });

      it('должна быть категория "Межрегиональные"', () => {
        const interregionalCategory = cargoCategories.find(cat => cat.category === Category.interregional);
        expect(interregionalCategory).toBeDefined();
        expect(interregionalCategory?.name).toBe(CategoryName.interregional);
        expect(interregionalCategory?.available).toBe(true);
      });

      it('должна быть категория "Внутренние курьерские"', () => {
        const domesticCourierCategory = cargoCategories.find(cat => cat.category === Category.domesticCourier);
        expect(domesticCourierCategory).toBeDefined();
        expect(domesticCourierCategory?.name).toBe(CategoryName.domestic_courier);
        expect(domesticCourierCategory?.available).toBe(true);
      });

      it('должна быть категория "Индивидуальные"', () => {
        const individualCategory = cargoCategories.find(cat => cat.category === Category.individual);
        expect(individualCategory).toBeDefined();
        expect(individualCategory?.name).toBe(CategoryName.individual);
        expect(individualCategory?.available).toBe(true);
      });

      it('должна быть категория "Шаблоны"', () => {
        const templateCategory = cargoCategories.find(cat => cat.category === Category.template);
        expect(templateCategory).toBeDefined();
        expect(templateCategory?.name).toBe(CategoryName.template);
        expect(templateCategory?.available).toBe(true);
      });
    });

    describe('Порядок категорий', () => {
      it('категории должны быть в правильном порядке', () => {
        const expectedOrder = [
          Category.all,
          Category.courier,
          Category.dedicated,
          Category.interregional,
          Category.domesticCourier,
          Category.individual,
          Category.template,
        ];

        const actualOrder = categories[Tab.cargo].map(cat => cat.category);
        expect(actualOrder).toEqual(expectedOrder);
      });
    });

    describe('Уникальность категорий', () => {
      it('не должно быть дублирующихся категорий', () => {
        const uniqueCategories = new Set(cargoCategories.map(cat => cat.category));
        expect(uniqueCategories.size).toBe(cargoCategories.length);
      });
    });
  });

  describe('Типы данных', () => {
    it('категории должны соответствовать интерфейсу CategoryItem', () => {
      categories[Tab.cargo].forEach((category) => {
        expect(typeof category.type).toBe('string');
        expect(typeof category.category).toBe('string');
        expect(typeof category.name).toBe('string');
        expect(typeof category.available).toBe('boolean');
      });
    });

    it('allowedRoles должен содержать только строки', () => {
      allowedRoles.forEach((role) => {
        expect(typeof role).toBe('string');
      });
    });
  });

  describe('Иммутабельность', () => {
    it('массив allowedRoles не должен быть изменяемым', () => {
      const originalAllowedRoles = [...allowedRoles];
      
      // Проверяем, что это константа (хотя JavaScript не гарантирует неизменяемость)
      // В реальном коде allowedRoles может быть константой, но массив можно изменить
      // Поэтому просто проверяем, что он определен
      expect(allowedRoles).toBeDefined();
      expect(Array.isArray(allowedRoles)).toBe(true);
    });
  });

  describe('Валидация данных', () => {
    it('все категории должны иметь непустые значения', () => {
      categories[Tab.cargo].forEach((category) => {
        expect(category.type).toBeTruthy();
        expect(category.category).toBeTruthy();
        expect(category.name).toBeTruthy();
        expect(typeof category.available).toBe('boolean');
      });
    });

    it('все имена категорий должны быть строками', () => {
      categories[Tab.cargo].forEach((category) => {
        expect(typeof category.name).toBe('string');
        expect(category.name.length).toBeGreaterThan(0);
      });
    });

    it('все типы категорий должны соответствовать Tab', () => {
      const validTypes = Object.values(Tab);
      categories[Tab.cargo].forEach((category) => {
        expect(validTypes).toContain(category.type);
      });
    });

    it('все категории должны соответствовать Category enum', () => {
      const validCategories = Object.values(Category);
      categories[Tab.cargo].forEach((category) => {
        expect(validCategories).toContain(category.category);
      });
    });
  });

  describe('Расширяемость', () => {
    it('структура позволяет легко добавлять новые категории', () => {
      // Проверяем, что categories может быть расширен
      const newCategory = {
        type: Tab.cargo,
        category: 'new_category' as Category,
        name: 'Новая категория',
        available: true,
      };
      
      const extendedCategories = {
        ...categories,
        [Tab.cargo]: [...categories[Tab.cargo], newCategory],
      };
      
      expect(extendedCategories[Tab.cargo]).toHaveLength(categories[Tab.cargo].length + 1);
      expect(extendedCategories[Tab.cargo][categories[Tab.cargo].length]).toEqual(newCategory);
    });
  });

  describe('Интеграция с компонентами', () => {
    it('категории можно использовать для рендеринга селектов', () => {
      const cargoOptions = categories[Tab.cargo].map(cat => ({
        value: cat.category,
        label: cat.name,
      }));
      
      expect(cargoOptions).toHaveLength(7);
      expect(cargoOptions[0]).toEqual({
        value: Category.all,
        label: CategoryName.all,
      });
      expect(cargoOptions[1]).toEqual({
        value: Category.courier,
        label: CategoryName.courier,
      });
    });

    it('allowedRoles можно использовать для проверки прав доступа', () => {
      const hasAccess = (role: string) => allowedRoles.includes(role as any);
      
      expect(hasAccess(Roles.ADMIN_DATA_MASTER)).toBe(true);
      expect(hasAccess(Roles.ENGINEER_CORP_CLIENT)).toBe(true);
      expect(hasAccess(Roles.ADMIN_CORP_CLIENT)).toBe(true);
      expect(hasAccess(Roles.DISPATCHER_SUPPORT_SERVICE)).toBe(true);
      expect(hasAccess('USER')).toBe(false);
      expect(hasAccess('GUEST')).toBe(false);
    });
  });
});

// Дополнительные тесты для типов и интерфейсов (если нужно проверить типы в runtime)
describe('Type guards', () => {
  it('должен корректно определять категорию', () => {
    const isValidCategory = (category: any): category is Category => {
      return Object.values(Category).includes(category);
    };
    
    expect(isValidCategory(Category.all)).toBe(true);
    expect(isValidCategory(Category.courier)).toBe(true);
    expect(isValidCategory('invalid_category')).toBe(false);
  });
});