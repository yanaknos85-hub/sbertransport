export interface TripSplitWarningModalProps {
  open: boolean;
  onDecline: VoidFunction;
  onAccept: VoidFunction;
  humanReadableId: string;
  id: string;
  status: string;
}
