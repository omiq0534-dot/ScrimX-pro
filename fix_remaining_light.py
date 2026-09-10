import os
import glob

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()
        
    old_content = content
    # Replace common dark card backgrounds
    content = content.replace('Color(0xFF14161F)', 'Color.White')
    content = content.replace('Color(0xFF111319)', 'Color.White')
    content = content.replace('Color(0xFF1E2130)', 'Color(0xFFF3F4F6)')
    content = content.replace('Color(0xFF1A1D27)', 'Color(0xFFF9FAFB)')
    content = content.replace('Color(0xFF262938)', 'Color(0xFFE5E7EB)')
    content = content.replace('Color(0xFF262A38)', 'AppColors.BorderColor')
    content = content.replace('Color(0xFF2D3244)', 'AppColors.BorderColor')
    content = content.replace('Color(0xFF2E3346)', 'AppColors.BorderColor')
    content = content.replace('Color(0xFF1E2430)', 'AppColors.BorderColor')
    content = content.replace('Color(0xFF0C0D12)', 'Color(0xFFF9FAFB)')
    content = content.replace('Color(0xFF222636)', 'Color(0xFFE5E7EB)')
    content = content.replace('Color(0xFF1C1F2B)', 'Color(0xFFF3F4F6)')
    
    # Replace text colors to contrast with white backgrounds
    content = content.replace('Color(0xFFE0E0E0)', 'Color.Black')
    content = content.replace('Color(0xFFD1D5DB)', 'Color(0xFF4B5563)')
    content = content.replace('Color(0xFFC0C4D6)', 'Color(0xFF4B5563)')
    
    if content != old_content:
        with open(filepath, "w") as f:
            f.write(content)
        print(f"Fixed {filepath}")

for root, _, files in os.walk("app/src/main/java/com/example/ui"):
    for file in files:
        if file.endswith(".kt"):
            fix_file(os.path.join(root, file))

print("All files updated for light theme")
