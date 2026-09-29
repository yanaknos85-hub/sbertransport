export interface AddAdditionalContactModalProps {
  visible: boolean;
  onClose: () => void;
  onConfirm: (data: {
    fullName: string;
    mobilePhone: string;
    employeeId: string;
    waypointId?: string;
  }[]) => void;
  waypointId: string;
}
