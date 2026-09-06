with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    lines = f.readlines()

def should_be_black(line_num):
    return False

for i in range(len(lines)):
    # lines to keep black:
    if i+1 in [97, 134, 136, 195, 199, 260, 272, 293, 341, 347, 414, 514, 526, 542, 918, 927, 933, 994, 998, 1039, 1201, 1229, 1381, 1385, 1872, 1875, 1877]:
        continue
    # restore to white
    lines[i] = lines[i].replace('color = Color.Black', 'color = Color.White')
    lines[i] = lines[i].replace('tint = Color.Black', 'tint = Color.White')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.writelines(lines)
