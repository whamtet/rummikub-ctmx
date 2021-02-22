(ns rummikub-ctmx.service.state
  (:require
    [clojure.set :as set]))

(def colors [:red :yellow :blue :black])
(def numbers (range 1 14))

(defn num-val [[_ num]]
  (cond num :joker 30 num))
(defn enumerate [s]
  (map-indexed list s))

(def tiles
  (concat
    (for [color colors number numbers _ (range 2)]
      [color number])
    [[:red :joker] [:black :joker]]))

(def state
  (atom
    {:pool (set tiles)
     :players {} ;; tile -> [player i j]
     :table {} ;; tile -> [x y]
     }))

(defn tiles-for-player [player {:keys [players]}]
  (for [[tile [name]] players :when (= name player)]
    tile))

(defn sort-tiles [player tiles]
  (let [{:keys [red yellow blue black]} (group-by first tiles)
        red (sort-by num-val red)
        yellow (sort-by num-val yellow)
        blue (sort-by num-val blue)
        black (sort-by num-val black)
        first-row (concat red yellow)
        second-row (concat blue black)]
    (into {}
          (for [[i row] (enumerate [first-row second-row])
                [j tile] (enumerate row)]
            [tile [player i j]]))))

(defn sort-player [state player]
  (->> state
       (tiles-for-player player)
       (sort-tiles player)
       (update state :players merge)))
(defn sort-player! [player]
  (swap! state sort-player player))

(defn pick-up [{:keys [pool players table]} player n]
  (let [to-pick (->> pool shuffle (take n))
        first-row (for [[tile [p i]] players :when (= [p i] [player 0])] tile)
        new-row (concat first-row to-pick)
        new-players (into {}
                          (for [[j tile] (enumerate new-row)]
                            [tile [player 0 j]]))]
    {:pool (set/difference pool (set to-pick))
     :players (merge players new-players)
     :table table}))
(defn pick-up! [player n]
  (swap! state pick-up player n))

(defn put-down [{:keys [pool players table]} tile x y]
  {:pool pool
   :players (dissoc players tile)
   :table (assoc table tile [x y])})
(defn put-down! [tile x y]
  (swap! state put-down tile x y))

(defn insert-at [s i tile]
  (let [[before after] (split-at i s)]
    (concat before [tile] after)))
(defn pick-up-used [{:keys [pool players table]} tile player i j]
  (let [insert-row (for [[tile [p id]] players :when (= [p id] [player i])] tile)
        new-row (insert-at insert-row j tile)
        new-players (into {}
                          (for [[j tile] (enumerate new-row)]
                            [tile [player i j]]))]
    {:pool pool
     :players (merge players new-players)
     :table (dissoc table tile)}))
(defn pick-up-used! [tile player i j]
  (swap! state pick-up-used tile player i j))
