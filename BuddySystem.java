import java.util.*;

class MemoryBlock {
    int size;
    boolean allocated;
    int startAddress;
    
    public MemoryBlock(int size, int startAddress) {
        this.size = size;
        this.allocated = false;
        this.startAddress = startAddress;
    }
}

public class BuddySystem {
    private List<MemoryBlock> memoryBlocks;
    private int totalMemorySize;
    
    public BuddySystem(int totalSize) {
        if (!isPowerOfTwo(totalSize)) {
            throw new IllegalArgumentException("Memory size must be a power of 2!");
        }
        this.totalMemorySize = totalSize;
        this.memoryBlocks = new ArrayList<>();
        memoryBlocks.add(new MemoryBlock(totalMemorySize, 0));
    }
    
    private boolean isPowerOfTwo(int n) {
        return n > 0 && ((n & (n - 1)) == 0);
    }
    
    private int getPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
    
    public boolean allocate(int requestedSize) {
        System.out.println("\n=== Allocating " + requestedSize + "KB ===");
        int size = getPowerOfTwo(requestedSize);
        System.out.println("Rounded up to nearest power of 2: " + size + "KB");
        
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (!block.allocated && block.size >= size) {
                System.out.println("Found suitable block of size " + block.size + "KB");
                while (block.size > size) {
                    int newSize = block.size / 2;
                    System.out.println("Splitting " + block.size + "KB block into two " + newSize + "KB blocks");
                    MemoryBlock newBlock = new MemoryBlock(newSize, block.startAddress + newSize);
                    memoryBlocks.add(i + 1, newBlock);
                    block.size = newSize;
                }
                block.allocated = true;
                System.out.println("Successfully allocated " + block.size + "KB at address " + block.startAddress);
                return true;
            }
        }
        System.out.println("Failed to allocate " + requestedSize + "KB - No suitable block found");
        return false;
    }
    
    public void free(int size, int address) {
        System.out.println("\n=== Freeing " + size + "KB at address " + address + " ===");
        size = getPowerOfTwo(size);
        
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (block.startAddress == address && block.size == size && block.allocated) {
                block.allocated = false;
                System.out.println("Block found and marked as free");
                System.out.println("Checking for possible buddy merges...");
                mergeBuddies();
                return;
            }
        }
        System.out.println("Block not found to free");
    }
    
    private void mergeBuddies() {
        boolean merged;
        do {
            merged = false;
            for (int i = 0; i < memoryBlocks.size() - 1; i++) {
                MemoryBlock current = memoryBlocks.get(i);
                MemoryBlock next = memoryBlocks.get(i + 1);
                
                if (!current.allocated && !next.allocated && 
                    current.size == next.size && 
                    (current.startAddress / current.size) % 2 == 0) {
                    
                    System.out.println("Found buddy blocks of size " + current.size + "KB at addresses " + 
                                     current.startAddress + " and " + next.startAddress);
                    System.out.println("Merging into a " + (current.size * 2) + "KB block");
                    current.size *= 2;
                    memoryBlocks.remove(i + 1);
                    merged = true;
                    break;
                }
            }
        } while (merged);
    }
    
    public void printMemoryState() {
        System.out.println("\nFinal Memory State:");
        for (MemoryBlock block : memoryBlocks) {
            System.out.println(block.size + "KB block at address " + block.startAddress + 
                             " - " + (block.allocated ? "Allocated" : "Free"));
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter memory size (must be a power of 2 in KB): ");
        int totalSize = scanner.nextInt();
        scanner.close();
        
        try {
            BuddySystem buddySystem = new BuddySystem(totalSize);
            
            // Initial state
            System.out.println("\n=== Initial State ===");
            buddySystem.printMemoryState();
            
            // Allocation Phase
            System.out.println("\n====== ALLOCATION PHASE ======");
            
            // Step 1: Allocate 100KB
            System.out.println("\nStep 1: First allocation of 100KB");
            buddySystem.allocate(100);
            buddySystem.printMemoryState();
            
            // Step 2: Allocate 200KB
            System.out.println("\nStep 2: Second allocation of 200KB");
            buddySystem.allocate(200);
            buddySystem.printMemoryState();
            
            // Deallocation Phase
            System.out.println("\n====== DEALLOCATION PHASE ======");
            
            // Step 3: Free 128KB
            System.out.println("\nStep 3: Free first allocation (128KB)");
            buddySystem.free(128, 0);
            buddySystem.printMemoryState();
            
            // Step 4: Free 256KB
            System.out.println("\nStep 4: Free second allocation (256KB)");
            buddySystem.free(256, 256);
            buddySystem.printMemoryState();
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
