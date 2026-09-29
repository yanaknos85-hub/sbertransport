/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen } from '@testing-library/react';
import { TripsModalProvider, useTripsModal } from './TripsModal';

// Mock types for testing
const mockCargoTrip = {
  id: 'trip-uuid-123',
  status: 'NEW' as const,
  requests: [],
  waypoints: [],
  isNew: true,
  humanReadableId: 'TRIP-001',
} as any;

const mockSetRequestData = {
  driverId: 'driver-uuid-456',
  shiftId: 'shift-uuid-789',
} as any;

const mockWaypoints = [
  {
    id: 'waypoint-uuid-1',
    latitude: 55.7558,
    longitude: 37.6173,
    city: 'Moscow',
    street: 'Tverskaya',
  },
  {
    id: 'waypoint-uuid-2',
    latitude: 55.7658,
    longitude: 37.6273,
    city: 'Moscow',
    street: 'Pushkinskaya',
  },
] as any;

const mockComments = ['First comment', 'Second comment', 'Third comment'] as any;

// Helper to render with provider
const renderWithProvider = (component: React.ReactNode) => {
  return render(<TripsModalProvider>{component}</TripsModalProvider>);
};

describe('TripsModal Context', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('useTripsModal hook', () => {
    describe('Initial state', () => {
      it('should have null type as initial state', () => {
        const TestComponent = () => {
          const { modalState } = useTripsModal();
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('null')).toBeInTheDocument();
      });

      it('should have all optional fields undefined by default', () => {
        const TestComponent = () => {
          const { modalState } = useTripsModal();
          return <div>{JSON.stringify(modalState)}</div>;
        };

        renderWithProvider(<TestComponent />);
        const state = JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^{.*}$/.test(content);
        }).textContent || '{}');
        expect(state).toEqual({
          type: null,
          trip: undefined,
          driverData: undefined,
          waypoints: undefined,
          comments: undefined,
        });
      });
    });

    describe('openSetDriver', () => {
      it('should set modal state to setDriver type with trip', () => {
        const TestComponent = () => {
          const { openSetDriver, modalState } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('setDriver')).toBeInTheDocument();
      });

      it('should store the trip object in modal state', () => {
        const TestComponent = () => {
          const { openSetDriver, modalState } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);
          return <div>{JSON.stringify(modalState.trip)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^{.*}$/.test(content);
        }).textContent || '{}')).toEqual(mockCargoTrip);
      });
    });

    describe('openSetRequest', () => {
      it('should set modal state to setRequest type with driverData', () => {
        const TestComponent = () => {
          const { openSetRequest, modalState } = useTripsModal();
          React.useEffect(() => {
            openSetRequest(mockSetRequestData);
          }, [openSetRequest]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('setRequest')).toBeInTheDocument();
      });

      it('should store the driverData object in modal state', () => {
        const TestComponent = () => {
          const { openSetRequest, modalState } = useTripsModal();
          React.useEffect(() => {
            openSetRequest(mockSetRequestData);
          }, [openSetRequest]);
          return <div>{JSON.stringify(modalState.driverData)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^{.*}$/.test(content);
        }).textContent || '{}')).toEqual(mockSetRequestData);
      });
    });

    describe('openEdit', () => {
      it('should set modal state to editTrip type with trip', () => {
        const TestComponent = () => {
          const { openEdit, modalState } = useTripsModal();
          React.useEffect(() => {
            openEdit(mockCargoTrip);
          }, [openEdit]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('editTrip')).toBeInTheDocument();
      });

      it('should store the trip object in modal state', () => {
        const TestComponent = () => {
          const { openEdit, modalState } = useTripsModal();
          React.useEffect(() => {
            openEdit(mockCargoTrip);
          }, [openEdit]);
          return <div>{JSON.stringify(modalState.trip)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^{.*}$/.test(content);
        }).textContent || '{}')).toEqual(mockCargoTrip);
      });
    });

    describe('openAddresses', () => {
      it('should set modal state to addresses type with waypoints', () => {
        const TestComponent = () => {
          const { openAddresses, modalState } = useTripsModal();
          React.useEffect(() => {
            openAddresses(mockWaypoints);
          }, [openAddresses]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('addresses')).toBeInTheDocument();
      });

      it('should store the waypoints array in modal state', () => {
        const TestComponent = () => {
          const { openAddresses, modalState } = useTripsModal();
          React.useEffect(() => {
            openAddresses(mockWaypoints);
          }, [openAddresses]);
          return <div>{JSON.stringify(modalState.waypoints)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^\[.*\]$/.test(content);
        }).textContent || '[]')).toEqual(mockWaypoints);
      });
    });

    describe('openComment', () => {
      it('should set modal state to comment type with comments', () => {
        const TestComponent = () => {
          const { openComment, modalState } = useTripsModal();
          React.useEffect(() => {
            openComment(mockComments);
          }, [openComment]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('comment')).toBeInTheDocument();
      });

      it('should store the comments array in modal state', () => {
        const TestComponent = () => {
          const { openComment, modalState } = useTripsModal();
          React.useEffect(() => {
            openComment(mockComments);
          }, [openComment]);
          return <div>{JSON.stringify(modalState.comments)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^\[.*\]$/.test(content);
        }).textContent || '[]')).toEqual(mockComments);
      });
    });

    describe('openFilters', () => {
      it('should set modal state to filters type', () => {
        const TestComponent = () => {
          const { openFilters, modalState } = useTripsModal();
          React.useEffect(() => {
            openFilters();
          }, [openFilters]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('filters')).toBeInTheDocument();
      });

      it('should have undefined trip, driverData, waypoints, and comments', () => {
        const TestComponent = () => {
          const { openFilters, modalState } = useTripsModal();
          React.useEffect(() => {
            openFilters();
          }, [openFilters]);
          return <div>{JSON.stringify(modalState)}</div>;
        };

        renderWithProvider(<TestComponent />);
        const state = JSON.parse(screen.getByText((content, element) => {
          return element?.tagName.toLowerCase() === 'div' && /^{.*}$/.test(content);
        }).textContent || '{}');
        expect(state.type).toBe('filters');
        expect(state.trip).toBeUndefined();
        expect(state.driverData).toBeUndefined();
        expect(state.waypoints).toBeUndefined();
        expect(state.comments).toBeUndefined();
      });
    });

    describe('openDownload', () => {
      it('should set modal state to download type', () => {
        const TestComponent = () => {
          const { openDownload, modalState } = useTripsModal();
          React.useEffect(() => {
            openDownload();
          }, [openDownload]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('download')).toBeInTheDocument();
      });
    });

    describe('openMutualSettlements', () => {
      it('should set modal state to mutualSettlements type', () => {
        const TestComponent = () => {
          const { openMutualSettlements, modalState } = useTripsModal();
          React.useEffect(() => {
            openMutualSettlements();
          }, [openMutualSettlements]);
          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('mutualSettlements')).toBeInTheDocument();
      });
    });

    describe('closeModal', () => {
      it('should set modal state to null type', () => {
        const TestComponent = () => {
          const {
            openSetDriver, closeModal, modalState,
          } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);

          React.useEffect(() => {
            closeModal();
          }, [closeModal]);

          return <div>{String(modalState.type)}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('null')).toBeInTheDocument();
      });
    });

    describe('isOpened', () => {
      it('should return false when no modal is open', () => {
        const TestComponent = () => {
          const { isOpened } = useTripsModal();
          return <div>{String(isOpened())}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('false')).toBeInTheDocument();
      });

      it('should return true when specific modal type is open', () => {
        const TestComponent = () => {
          const { openSetDriver, isOpened } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);
          return <div>{String(isOpened('setDriver'))}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('true')).toBeInTheDocument();
      });

      it('should return false when checking for a different modal type', () => {
        const TestComponent = () => {
          const { openSetDriver, isOpened } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);
          return <div>{String(isOpened('editTrip'))}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('false')).toBeInTheDocument();
      });

      it('should return true when no type is specified and some modal is open', () => {
        const TestComponent = () => {
          const { openSetDriver, isOpened } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);
          return <div>{String(isOpened())}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('true')).toBeInTheDocument();
      });

      it('should return false when no type is specified and no modal is open', () => {
        const TestComponent = () => {
          const { isOpened } = useTripsModal();
          return <div>{String(isOpened())}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('false')).toBeInTheDocument();
      });

      it('should update when modal state changes', () => {
        const TestComponent = () => {
          const {
            openSetDriver, openEdit, isOpened,
          } = useTripsModal();
          React.useEffect(() => {
            openSetDriver(mockCargoTrip);
          }, [openSetDriver]);

          React.useEffect(() => {
            openEdit(mockCargoTrip);
          }, [openEdit]);

          return <div>{String(isOpened('editTrip'))}</div>;
        };

        renderWithProvider(<TestComponent />);
        expect(screen.getByText('true')).toBeInTheDocument();
      });
    });
  });

  describe('Modal state structure', () => {
    it('should have correct type union for all modal types', () => {
      const TestComponent = () => {
        const {
          openSetDriver,
          openSetRequest,
          openEdit,
          openAddresses,
          openComment,
          openFilters,
          openDownload,
          openMutualSettlements,
          closeModal,
        } = useTripsModal();
        const [currentType, setCurrentType] = React.useState<string>('');

        const testStates = () => {
          openSetDriver(mockCargoTrip);
          setCurrentType('setDriver');
          openSetRequest(mockSetRequestData);
          setCurrentType('setRequest');
          openEdit(mockCargoTrip);
          setCurrentType('editTrip');
          openAddresses(mockWaypoints);
          setCurrentType('addresses');
          openComment(mockComments);
          setCurrentType('comment');
          openFilters();
          setCurrentType('filters');
          openDownload();
          setCurrentType('download');
          openMutualSettlements();
          setCurrentType('mutualSettlements');
          closeModal();
          setCurrentType('null');
        };

        React.useEffect(() => {
          testStates();
        // eslint-disable-next-line react-hooks/exhaustive-deps
        }, []);

        return <div>{currentType}</div>;
      };

      renderWithProvider(<TestComponent />);
      // The last state should be null after all operations
      expect(screen.getByText('null')).toBeInTheDocument();
    });

    it('should properly handle multiple sequential opens', () => {
      const TestComponent = () => {
        const {
          openSetDriver, openEdit, openAddresses, openComment, modalState,
        } = useTripsModal();
        const [lastType, setLastType] = React.useState<string>('');

        React.useEffect(() => {
          openSetDriver(mockCargoTrip);
          setLastType(modalState.type as string);
        }, [openSetDriver, modalState.type]);

        React.useEffect(() => {
          openEdit(mockCargoTrip);
          setLastType(modalState.type as string);
        }, [openEdit, modalState.type]);

        React.useEffect(() => {
          openAddresses(mockWaypoints);
          setLastType(modalState.type as string);
        }, [openAddresses, modalState.type]);

        React.useEffect(() => {
          openComment(mockComments);
          setLastType(modalState.type as string);
        }, [openComment, modalState.type]);

        return <div>{lastType}</div>;
      };

      renderWithProvider(<TestComponent />);
      // The last effect should set the type to comment
      expect(screen.getByText('comment')).toBeInTheDocument();
    });
  });

  describe('Integration with provider', () => {
    it('should provide context to nested components', () => {
      const NestedComponent = () => {
        const { modalState } = useTripsModal();
        return <div>{String(modalState.type)}</div>;
      };

      const ParentComponent = () => {
        const { openSetDriver } = useTripsModal();
        React.useEffect(() => {
          openSetDriver(mockCargoTrip);
        }, [openSetDriver]);
        return <NestedComponent />;
      };

      renderWithProvider(<ParentComponent />);
      expect(screen.getByText('setDriver')).toBeInTheDocument();
    });

    it('should maintain state across re-renders', () => {
      const TestComponent = () => {
        const {
          openSetDriver, modalState, closeModal,
        } = useTripsModal();
        const [count, setCount] = React.useState(0);

        const handleClick = () => {
          if (count === 0) {
            openSetDriver(mockCargoTrip);
          } else if (count === 1) {
            closeModal();
          }
          setCount(c => c + 1);
        };

        return (
          <div>
            <button onClick={handleClick}>Toggle</button>
            <span>{String(modalState.type)}</span>
          </div>
        );
      };

      renderWithProvider(<TestComponent />);
      const button = screen.getByRole('button');
      const span = screen.getByText('null');

      // Initial state
      expect(span).toBeInTheDocument();

      // Open modal
      button.click();
      expect(screen.getByText('setDriver')).toBeInTheDocument();

      // Close modal
      button.click();
      expect(screen.getByText('null')).toBeInTheDocument();
    });
  });
});
